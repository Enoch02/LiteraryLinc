package com.enoch02.reader.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.enoch02.reader.models.PdfFile

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
 * Generate thumbnail from the first page of the document
 * */
//TODO: make it generate thumbnails for various kinds of documents
fun generateThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        parcelFileDescriptor?.use { pfd ->
            val pdfRenderer = PdfRenderer(pfd)
            val page = pdfRenderer.openPage(0)

            val width = page.width
            val height = page.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            pdfRenderer.close()
            bitmap
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


/**
 * Get files from app directory
 */
fun listPdfFilesInDirectory(context: Context, directoryUri: Uri): List<PdfFile> {
    val foundFiles = mutableListOf<PdfFile>()

    directoryUri.let { uri ->
        val documentFile = DocumentFile.fromTreeUri(context, uri)

        documentFile?.let { dir ->
            if (dir.isDirectory) {
                val files = dir.listFiles()
                for (file in files) {
                    if (file.isFile && allowedTypes.contains(file.type)) {
                        val fileUri = file.uri
                        val fileName = file.name ?: "Unknown"

                        val thumbnail = generateThumbnail(context, fileUri)

                        foundFiles.add(
                            PdfFile(
                                contentUri = fileUri,
                                name = fileName,
                                thumbnail = thumbnail
                            )
                        )
                    }
                }
            }
        }
    }

    return foundFiles
}

