package com.enoch02.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun getFileFromUri(context: Context, uri: Uri): Result<File> {
    val contentResolver = context.contentResolver
    val fileName = contentResolver.query(uri, null, null, null, null).use { cursor ->
        val name = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        name?.let { cursor.getString(it) } ?: ""
    }

    try {
        val input = contentResolver.openInputStream(uri)
        if (input != null) {
            val outputDir = File(context.cacheDir, "temp")
            outputDir.mkdirs()

            val outputFile = File(outputDir, fileName)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(4 * 1024)
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            outputStream.close()
            input.close()

            return Result.success(outputFile)
        }
    } catch (e: IOException) {
        return Result.failure(e)
    }

    return Result.failure(IOException("File not found"))
}