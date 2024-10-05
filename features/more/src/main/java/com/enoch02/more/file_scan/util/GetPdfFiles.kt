package com.enoch02.more.file_scan.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.enoch02.database.model.Document

val allowedTypes = arrayOf(
    "application/pdf",
    "application/vnd.ms-xpsdocument",
    "application/oxps",
    "application/x-cbz",
    "application/vnd.comicbook+zip",
    "application/epub+zip",
    "application/x-fictionbook",
    "application/x-mobipocket-ebook",
)

/**
 * Get files from app directory
 */
fun listDocsInDirectory(context: Context, directoryUri: Uri): List<Document> {
    val foundFiles = mutableListOf<Document>()

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

            foundFiles.add(
                Document(
                    id = file.getDocumentFileMd5(context.contentResolver).toString(),
                    contentUri = fileUri,
                    name = nameWithoutExtension
                )
            )
        } else if (file.isDirectory) {
            // Recursive call for subdirectories
            foundFiles.addAll(listDocsInDirectory(context, file.uri))
        }
    }

    return foundFiles
}
