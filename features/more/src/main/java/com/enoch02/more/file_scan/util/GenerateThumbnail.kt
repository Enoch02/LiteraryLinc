package com.enoch02.more.file_scan.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import nl.siegmann.epublib.epub.EpubReader
import java.util.Locale
import java.util.zip.ZipInputStream

/**
 * Generates a thumbnail from the first image in supported file Uri.
 * Supported: pdf, epub, cbz
 *
 * @param context The context for accessing the content resolver.
 * @return A Bitmap of the first image or null if no image is found.
 */
fun Uri.generateThumbnail(context: Context): Bitmap? {
    return try {
        when (getFileExtension(this)) {
            "pdf" -> generatePdfThumbnail(context, this)
            "epub" -> generateEpubThumbnail(context, this)
            "cbz" -> generateCbzThumbnail(context, this)
            else -> null
        }
    } catch (e: Exception) {
        Log.e("GenerateThumbExtension", "generateThumbnail: ")
        null
    }
}

/**
 * Generates a thumbnail from the first image in a PDF file.
 *
 * @param context The context for accessing the content resolver.
 * @param uri The Uri of the CBZ file.
 * @return A Bitmap of the first image or null if no image is found.
 */
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

/**
 * Generates a thumbnail from the first image in an EPUB file.
 *
 * @param context The context for accessing the content resolver.
 * @param uri The Uri of the CBZ file.
 * @return A Bitmap of the first image or null if no image is found.
 */
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

/**
 * Generates a thumbnail from the first image in a CBZ file.
 *
 * @param context The context for accessing the content resolver.
 * @param uri The Uri of the CBZ file.
 * @return A Bitmap of the first image or null if no image is found.
 */
private fun generateCbzThumbnail(context: Context, uri: Uri): Bitmap? {
    val contentResolver = context.contentResolver

    // Open the CBZ (ZIP) file from the Uri
    contentResolver.openInputStream(uri)?.use { inputStream ->
        ZipInputStream(inputStream).use { zipStream ->
            var entry = zipStream.nextEntry

            // Loop through the entries in the zip file
            while (entry != null) {
                if (entry.name.endsWith(".png", ignoreCase = true) ||
                    entry.name.endsWith(".jpg", ignoreCase = true) ||
                    entry.name.endsWith(".jpeg", ignoreCase = true)
                ) {
                    // Decode and return the first image found
                    return BitmapFactory.decodeStream(zipStream)
                }

                // Move to the next entry in the CBZ
                entry = zipStream.nextEntry
            }
        }
    }

    return null
}

private fun getFileExtension(uri: Uri): String {
    return uri.path?.substringAfterLast('.', "")?.lowercase(Locale.ROOT) ?: ""
}