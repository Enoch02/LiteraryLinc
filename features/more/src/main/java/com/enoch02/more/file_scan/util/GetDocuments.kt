package com.enoch02.more.file_scan.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.artifex.mupdf.fitz.SeekableInputStream
import com.artifex.mupdf.viewer.ContentInputStream
import com.artifex.mupdf.viewer.MuPDFCore
import com.enoch02.database.model.LLDocument
import com.enoch02.more.file_scan.TAG

val allowedTypes = arrayOf(
    "application/pdf",
    "application/epub+zip",
    /*TODO*/
    /*"application/vnd.ms-xpsdocument",
    "application/oxps",
    "application/x-cbz",
    "application/vnd.comicbook+zip",
    "application/x-fictionbook",
    "application/x-mobipocket-ebook",*/
)

/**
 * Get files from app directory
 */
fun listDocsInDirectory(context: Context, directoryUri: Uri): List<LLDocument> {
    val foundFiles = mutableListOf<LLDocument>()

    val documentFile = DocumentFile.fromTreeUri(context, directoryUri)
    if (documentFile == null || !documentFile.isDirectory) {
        Log.e("DocumentFile", "Invalid directory URI or not a directory.")
        return foundFiles // Early exit if invalid
    }

    val files = documentFile.listFiles()
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
                    lastRead = null,
                    type = fileName.substringAfterLast(".").uppercase()
                )
            )
        } else if (file.isDirectory) {
            // Recursive call for subdirectories
            foundFiles.addAll(listDocsInDirectory(context, file.uri))
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

private fun getDocumentMetadata(context: Context, uri: Uri): DocumentMetadata? {
    var cursor: Cursor? = null
    val mimeType = context.contentResolver.getType(uri).toString()
    var fileSize = 0L

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
        // and size (unless one was decoded
    } finally {
        cursor?.close()
    }

    val core = openStream(ContentInputStream(context.contentResolver, uri, fileSize), mimeType)
    if (core != null) {
        return DocumentMetadata(
            author = core.author,
            title = core.title,
            sizeInMb = if (fileSize > 0L) fileSize.toDouble() / (1024 * 1024) else 0.0,
            pages = core.countPages(),
            currentPage = core.currentPage
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
