package com.artifex.mupdf.viewer

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.fitz.Matrix
import com.artifex.mupdf.fitz.Outline
import com.artifex.mupdf.fitz.Page
import com.artifex.mupdf.fitz.android.AndroidDrawDevice
import com.artifex.mupdf.viewer.components.BitmapManager
import com.artifex.mupdf.viewer.components.ContentState
import com.artifex.mupdf.viewer.components.cleanup
import com.artifex.mupdf.viewer.old.ContentInputStream
import com.artifex.mupdf.viewer.shared.Item
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.LLDocument
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Instant
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

private const val TAG = "LL"

@HiltViewModel
class LLReaderViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val documentDao: DocumentDao,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val bitmapManager = BitmapManager.getInstance()

    var contentState by mutableStateOf(ContentState.LOADING)
    var document by mutableStateOf<Document?>(null)
    var currentPage by mutableIntStateOf(0)
    val pages = mutableStateListOf<Page>()
    var hasOutline by mutableStateOf(false)
    val flatOutline = mutableStateListOf<Item>()

    var docTitle by mutableStateOf("")
    var docKey = ""
    var size: Long = -1

    private var scale = 2f
    private var documentPageCount = 0
    private var documentId: String? = null

    private val pageRunLock = Any()
    private val updateJobMutex = Mutex()

    fun initDocument(context: Context, uri: Uri, mimeType: String?, id: String?) {
        var cursor: Cursor? = null

        docKey = uri.toString()
        scale = context.resources.displayMetrics.density
        documentId = id

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
            try {
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
                    Log.i(TAG, "  Opening document from memory buffer of size " + buf.size)
                    document = Document.openDocument(buf, mimetype)
                } else {
                    Log.i(TAG, "  Opening document from stream")
                    document = Document.openDocument(
                        ContentInputStream(
                            cr,
                            uri,
                            size
                        ), mimetype
                    )
                }

                documentPageCount = document!!.countPages()
                getCurrentPage()
                loadOutline()
                loadPages()
                contentState = ContentState.NOT_LOADING
            } catch (_: Exception) {
                // catch exceptions that occurs when user closes the screen
                // before a document loads
            }
        }
    }

    private fun loadPages() {
        try {
            if (document != null) {
                for (i in 0..<documentPageCount) {
                    val page = document!!.loadPage(i)
                    pages.add(page)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadPages: ${e.message}")
        }
    }

    fun getPageBitmap(index: Int): Flow<Bitmap?> = flow {
        val pageKey = "${docTitle}-$index"
        Log.d(TAG, "getPageBitmap: loading page $pageKey")
        if (document != null) {
            val cachedBitmap = bitmapManager.getCachedBitmap(pageKey)

            if (cachedBitmap != null && !cachedBitmap.isRecycled) {
                Log.i(TAG, "getPageBitmap: using cached bitmap!")
                emit(cachedBitmap)

            } else {
                Log.i(TAG, "getPageBitmap: page not in cache")
                bitmapManager.releaseBitmap(cachedBitmap)
                try {
                    val page: Page = pages[index]
                    val bounds = page.bounds
                    val ctm = Matrix()
                    ctm.scale(scale)
                    val bitmap = Bitmap.createBitmap(
                        (bounds.x1 * scale).toInt(),
                        (bounds.y1 * scale).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    val device = AndroidDrawDevice(bitmap)

                    synchronized(pageRunLock) {
                        page.run(device, ctm, null)
                    }
                    device.close()
                    bitmapManager.cacheBitmap(pageKey, bitmap)

                    emit(bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "getPageBitmap at index $index: ${e.message}")
                    e.printStackTrace()
                    emit(null)
                }
            }
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun getCurrentPage() {
        val doc = documentId?.let { documentDao.getDocument(it) }
        val book = documentId?.let { bookDao.getBookByMd5(it) }

        if (doc != null) {
            if (docTitle.isEmpty()) {
                docTitle = doc.name
            }

            if (book != null) {
                currentPage = if (doc.autoTrackable) {
                    book.pagesRead
                } else {
                    doc.currentPage
                }

                // synchronize currentPage with zero indexing expected by the pager
                if (currentPage > 0) {
                    currentPage--
                }
            }
        }
    }

    private suspend fun loadOutline() {
        withContext(Dispatchers.IO) {
            try {
                val outline = document?.loadOutline()

                if (outline == null) {
                    throw Exception()
                } else {
                    flattenOutlineNodes(outline, "")
                    hasOutline = true
                }

                flatOutline.sortBy { it.page }
            } catch (e: Exception) {
                Log.e(TAG, "loadOutline: $docTitle has no outline")
            }
        }
    }

    private fun flattenOutlineNodes(
        list: Array<Outline>,
        indent: String
    ) {
        if (document != null) {
            for (node in list) {
                if (node.title != null) {
                    val page = document!!.pageNumberFromLocation(document!!.resolveLink(node))
                    flatOutline.add(Item(indent + node.title, page, node.uri))
                }
                if (node.down != null) flattenOutlineNodes(node.down, "$indent    ")
            }
        }
    }

    fun getChapterStart(): Int {
        var chapterStart = -1

        if (document != null) {
            if (document!!.isPDF) {
                for (item in flatOutline) {
                    val page = document!!.resolveLink(item.uri).page

                    if (page <= currentPage) {
                        chapterStart = page
                    } else {
                        break
                    }
                }
            } else {
                val location = document!!.locationFromPageNumber(currentPage)
                var testPage = currentPage

                // Search backwards until we find a different chapter or reach page 0
                while (testPage > 0) {
                    val prevLocation = document!!.locationFromPageNumber(testPage - 1)
                    if (prevLocation.chapter != location.chapter) {
                        return testPage
                    }
                    testPage--
                }

                return 0 // chapter starts at page 0
            }
        }

        return chapterStart
    }

    /**
     * Update stored document info
     */
    fun updateDocumentData() {
        viewModelScope.launch(Dispatchers.IO) {
            updateJobMutex.withLock {
                val doc = documentId?.let { documentDao.getDocument(it) }
                if (doc != null) {
                    val lastRead =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Date.from(Instant.now())
                        } else {
                            Calendar.getInstance().time
                        }

                    // adding 1 because the pager and lib are zero indexed
                    val modifiedDocument = doc.copy(
                        pages = documentPageCount,
                        currentPage = currentPage + 1,
                        lastRead = lastRead,
                        isRead = (currentPage + 1) == documentPageCount
                    )

                    documentDao.updateDocument(modifiedDocument)

                    if (doc.autoTrackable) {
                        updateBookListEntry(modifiedDocument)
                    }
                }
            }
        }
    }

    /**
     * Update an item in the book list with updated progress whenever
     * a document is closed
     *
     * @param document the md5 of the document
     * */
    private fun updateBookListEntry(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            val book = bookDao.getBookByMd5(document.id)
            val isComplete = (document.currentPage + 1) == document.pages
            val completionDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Instant.now().toEpochMilli()
            } else {
                Calendar.getInstance().time.time
            }

            book?.let { theBook ->
                if (theBook.pagesRead <= document.currentPage) {
                    bookDao.updateBook(
                        theBook.copy(
                            pageCount = document.pages,
                            pagesRead = document.currentPage,
                            coverImageName = if (theBook.coverImageName.isNullOrEmpty()) document.cover else theBook.coverImageName,
                            status = if (document.pages == document.currentPage) Book.status[1] else Book.status[0],
                            dateCompleted = if (isComplete && theBook.dateCompleted == null) {
                                completionDate
                            } else {
                                theBook.dateCompleted
                            }
                        )
                    )
                }
            }
        }
    }

    fun getBooleanPreference(key: SettingsRepository.PreferenceType): Flow<Boolean> {
        return settingsRepository.getBooleanPreference(key)
    }

    override fun onCleared() {
        super.onCleared()
        document?.destroy()

        viewModelScope.launch {
            bitmapManager.cleanup()
        }
    }
}