package com.enoch02.more.file_scan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import nl.siegmann.epublib.epub.EpubReader
import java.util.Locale

fun generateThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        when (getFileExtension(uri)) {
            "pdf" -> generatePdfThumbnail(context, uri)
            "epub" -> generateEpubThumbnail(context, uri)
            else -> null
        }
    } catch (e: Exception) {
        //e.printStackTrace()
        null
    }
}

private fun generatePdfThumbnail(context: Context, uri: Uri): Bitmap? {
    val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
    return parcelFileDescriptor?.use { pfd ->
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
}

private fun generateEpubThumbnail(context: Context, uri: Uri): Bitmap? {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val epubReader = EpubReader()
        val book = epubReader.readEpub(inputStream)

        // Try to get the cover image
        val coverImage = book.coverImage
        if (coverImage != null) {
            return BitmapFactory.decodeStream(coverImage.inputStream)
        }

        // If no cover image, try to get the first image from the book
        for (resource in book.contents) {
            if (resource.mediaType.toString().startsWith("image/")) {
                return BitmapFactory.decodeStream(resource.inputStream)
            }
        }
    }
    return null
}

private fun getFileExtension(uri: Uri): String {
    return uri.path?.substringAfterLast('.', "")?.lowercase(Locale.ROOT) ?: ""
}