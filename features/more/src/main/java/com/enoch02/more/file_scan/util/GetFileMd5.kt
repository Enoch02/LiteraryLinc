package com.enoch02.more.file_scan.util

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.InputStream
import java.security.MessageDigest

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