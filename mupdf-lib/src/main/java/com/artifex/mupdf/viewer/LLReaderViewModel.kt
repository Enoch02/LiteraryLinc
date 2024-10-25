package com.artifex.mupdf.viewer

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import com.artifex.mupdf.viewer.components.ContentState
import com.artifex.mupdf.viewer.old.ContentInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class LLReaderViewModel : ViewModel() {
    private val app = "LL"

    var contentState by mutableStateOf(ContentState.LOADING)
    var document by mutableStateOf<Document?>(null)
    var currentPage by mutableIntStateOf(1)
    var currentPageBitmap: Bitmap? by mutableStateOf(null)

    var docTitle = ""
    var docKey = ""
    var size: Long = -1
    private var scale = 2f

    fun initDocument(context: Context, uri: Uri, mimeType: String?) {
        docKey = uri.toString()
        var cursor: Cursor? = null
        scale = context.resources.displayMetrics.density;

        try {
            cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                var idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_STRING)
                    docTitle = cursor.getString(idx)

                idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_INTEGER)
                    size = cursor.getLong(idx)

                if (size == 0L) size = -1
            }
        } catch (x: Exception) {
            // Ignore any exception and depend on default values for title
            // and size (unless one was decoded
        } finally {
            cursor?.close()
        }

        if (mimeType == null || mimeType == "application/octet-stream") {
            openDocument(context, uri, size, context.contentResolver.getType(uri) ?: docTitle)
        } else {
            openDocument(context, uri, size, mimeType)
        }
    }

    @Throws(IOException::class)
    private fun openDocument(context: Context, uri: Uri, size: Long, mimetype: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val cr: ContentResolver = context.contentResolver
            val inputStream = cr.openInputStream(uri)
            var buf: ByteArray? = null
            var used = -1
            try {
                val limit = 8 * 1024 * 1024
                if (size < 0) { // size is unknown
                    buf = ByteArray(limit)
                    used = inputStream!!.read(buf)
                    val atEOF = inputStream.read() == -1
                    if (used < 0 || (used == limit && !atEOF))  // no or partial data
                        buf = null
                } else if (size <= limit) { // size is known and below limit
                    buf = ByteArray(size.toInt())
                    used = inputStream!!.read(buf)
                    if (used < 0 || used < size)  // no or partial data
                        buf = null
                }
                if (buf != null && buf.size != used) {
                    val newbuf = ByteArray(used)
                    System.arraycopy(buf, 0, newbuf, 0, used)
                    buf = newbuf
                }
            } catch (e: OutOfMemoryError) {
                buf = null
            } finally {
                inputStream!!.close()
            }

            if (buf != null) {
                Log.i(app, "  Opening document from memory buffer of size " + buf.size)
                document = Document.openDocument(buf, mimetype)
            } else {
                Log.i(app, "  Opening document from stream")
                document = Document.openDocument(
                    ContentInputStream(
                        cr,
                        uri,
                        size
                    ), mimetype
                )
            }

            getPageBitmap()
        }
    }


    //TODO: optimize function.
    fun getPageBitmap() {
        viewModelScope.launch(Dispatchers.IO) {
            if (document != null) {
                val page = document!!.loadPage(currentPage - 1)
                val bounds = page.bounds

                val ctm = Matrix()
                ctm.scale(scale)
                val bitmap =
                    Bitmap.createBitmap(
                        (bounds.x1 * scale).toInt(),
                        (bounds.y1 * scale).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                val device = AndroidDrawDevice(bitmap)

                page.run(device, ctm, null);

                currentPageBitmap = bitmap
                contentState = ContentState.NOT_LOADING
            }
        }
    }

    fun nextPage() {
        if (document != null) {
            if (currentPage <= document?.countPages()!!) {
                currentPage++
                getPageBitmap()
            }
        }
    }

    fun previousPage() {
        if (currentPage != 1) {
            currentPage--
            getPageBitmap()
        }
    }
}