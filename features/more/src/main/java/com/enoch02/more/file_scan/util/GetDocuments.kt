package com.enoch02.more.file_scan.util

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.artifex.mupdf.fitz.SeekableInputStream
import com.artifex.mupdf.viewer.old.ContentInputStream
import com.artifex.mupdf.viewer.old.MuPDFCore
import com.enoch02.database.model.LLDocument
import com.enoch02.more.file_scan.TAG
import java.io.InputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import kotlin.math.roundToInt

val allowedTypes = arrayOf(
    "application/pdf",
    "application/epub+zip",
    "application/x-cbz",
    "application/vnd.comicbook+zip",
    /*TODO*/
    /*"application/vnd.ms-xpsdocument",
    "application/oxps",
    "application/x-fictionbook",
    "application/x-mobipocket-ebook",*/
)

/**
 * Get files from specified app directory
 *
 * @param context The context for accessing the content resolver.
 * @param directoryUri The Uri of the directory to scan.
 * @return a list of [LLDocument]
 */
fun listDocsInDirectory(
    context: Context,
    directoryUri: Uri,
    scanned: List<Uri?>
): List<LLDocument> {
    val foundFiles = mutableListOf<LLDocument>()

    val documentFile = DocumentFile.fromTreeUri(context, directoryUri)
    if (documentFile == null || !documentFile.isDirectory) {
        Log.e("DocumentFile", "Invalid directory URI or not a directory.")
        return foundFiles // Early exit if invalid
    }

    val files = documentFile
        .listFiles()
        .filterNot { df -> scanned.contains(df.uri) && df.exists() }

    if (files.isEmpty()) {
        return foundFiles // Early exit if no files
    }

    for (file in files) {
        if (file.isFile && allowedTypes.contains(file.type)) {
            val fileUri = file.uri
            val fileName = file.name ?: "Unknown"
            val nameWithoutExtension = fileName.substringBeforeLast(".")
            val metadata = getDocumentMetadata(context, fileUri)
            val documentName =
                if (metadata?.title.isNullOrEmpty()) nameWithoutExtension else metadata?.title

            foundFiles.add(
                LLDocument(
                    id = file.getDocumentFileMd5(context.contentResolver).toString(),
                    contentUri = fileUri,
                    name = documentName.toString(),
                    author = metadata?.author ?: "",
                    pages = metadata?.pages ?: 0,
                    currentPage = metadata?.currentPage ?: 0,
                    sizeInMb = metadata?.sizeInMb ?: 0.0,
                    lastRead = getCurrentDate(),
                    type = fileName.substringAfterLast(".").uppercase()
                )
            )
        } else if (file.isDirectory) {
            // Recursive call for subdirectories
            foundFiles.addAll(listDocsInDirectory(context, file.uri, scanned))
        }
    }

    return foundFiles
}

data class DocumentMetadata(
    val author: String,
    val title: String,
    val sizeInMb: Double,
    val pages: Int,
    val currentPage: Int
)

/**
 * Obtains document metadata using [MuPDFCore]
 *
 * @param context The context for accessing the content resolver.
 * @param uri The Uri of the document file.
 * @return [DocumentMetadata] if data is available or null
 */
private fun getDocumentMetadata(context: Context, uri: Uri): DocumentMetadata? {
    var cursor: Cursor? = null
    val mimeType = context.contentResolver.getType(uri).toString()
    var fileSize = 0L
    fun roundToTwoDecimalPlaces(number: Double): Double {
        return (number * 100).roundToInt() / 100.0
    }

    try {
        cursor = context.contentResolver.query(uri, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (idx >= 0 && cursor.getType(idx) == Cursor.FIELD_TYPE_INTEGER) {
                fileSize = cursor.getLong(idx)
            }
        }
    } catch (e: Exception) {
        // Ignore any exception and depend on default values for title
        // and size unless one was decoded
    } finally {
        cursor?.close()
    }

    val core = openStream(
        ContentInputStream(
            context.contentResolver,
            uri,
            fileSize
        ), mimeType)
    val fileSizeInMb = if (fileSize > 0L) fileSize.toDouble() / (1024 * 1024) else 0.0

    if (core != null) {
        return DocumentMetadata(
            author = core.author,
            title = core.title,
            sizeInMb = roundToTwoDecimalPlaces(fileSizeInMb),
            pages = core.countPages(),
            currentPage = max(core.currentPage, 0)
        )
    }

    return null
}

private fun openStream(stm: SeekableInputStream, magic: String): MuPDFCore? {
    val core: MuPDFCore?

    try {
        core = MuPDFCore(stm, magic)
    } catch (e: Exception) {
        Log.e(TAG, "Error opening document stream: $e")
        return null
    }
    return core
}

/**
 * Generate document file MD5
 *
 * @param contentResolver The content resolver.
 * @return A [String] of the MD5 or null if no MD5 can be obtained.
 */
fun DocumentFile.getDocumentFileMd5(contentResolver: ContentResolver): String? {
    try {
        // Create an MD5 digest instance
        val md = MessageDigest.getInstance("MD5")

        // Use ContentResolver to open an InputStream for the DocumentFile
        val inputStream: InputStream? = contentResolver.openInputStream(this.uri)

        inputStream.use { fis ->
            if (fis != null) {
                val buffer = ByteArray(1024)
                var bytesRead: Int

                // Read the file in chunks and update the digest
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
        }

        // Convert the digest to a hex string
        val digest = md.digest()
        return digest.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

private fun getCurrentDate(): Date {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Date.from(Instant.now())
    } else {
        Calendar.getInstance().time
    }
}
