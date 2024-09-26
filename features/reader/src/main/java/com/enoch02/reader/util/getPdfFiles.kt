package com.enoch02.reader.util

import android.content.Context
import android.net.Uri
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
fun listPdfFilesInDirectory(context: Context, directoryUri: Uri): List<Document> {
    val foundFiles = mutableListOf<Document>()

    directoryUri.let { uri ->
        val documentFile = DocumentFile.fromTreeUri(context, uri)

        documentFile?.let { dir ->
            if (dir.isDirectory) {
                val files = dir.listFiles()
                for (file in files) {
                    if (file.isFile && allowedTypes.contains(file.type)) {
                        val fileUri = file.uri
                        val fileName = file.name ?: "Unknown"

                        foundFiles.add(
                            Document(
                                contentUri = fileUri,
                                name = fileName
                            )
                        )
                    }
                }
            }
        }
    }

    return foundFiles
}

