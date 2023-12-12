package com.enoch02.coverfile

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.enoch02.util.getFileFromUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

private const val TAG = "COVER_REPO"

class BookCoverRepository(private val context: Context) {
    private val coverFolder = File(context.filesDir.path, "covers/")

    val latestCoverPath: Flow<Map<String, String?>> = flow {
        while (true) {
            val latestPathMap = mutableMapOf<String, String?>()
            coverFolder.listFiles()?.forEach {
                latestPathMap[it.name] = it.absolutePath
            }
            emit(latestPathMap.toMap())
            delay(5000)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun copyCover(coverUri: Uri): String {
        val coverFile = getFileFromUri(context, coverUri)
            .onSuccess { file ->
                if (!coverFolder.exists()) {
                    coverFolder.mkdir()
                }

                val fileNames = coverFolder.listFiles()?.map { it.name } ?: emptyList()

                if (file.name !in fileNames) {
                    Compressor.compress(context = context, imageFile = file) {
                        default()
                        destination(File(coverFolder.path, file.name))
                    }
                }
            }
            .onFailure {
                Log.e(TAG, "copyCover: ${it.message}")
            }

        return coverFile.getOrThrow().name
    }

    fun deleteAllCovers() {
        val files = coverFolder.listFiles()?.toList()

        files?.forEach { file ->
            file.delete()
            Log.d(TAG, "${file.name} deleted!")
        }
    }

    fun deleteOneCover(coverName: String) {
        try {
            if (coverName.isNotEmpty()) {
                val file = File(coverFolder, coverName)
                file.delete()
                Log.d(TAG, "$coverName deleted!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteOneCover() -> $e")
        }
    }

    //TODO: DEBUG!
    fun downloadCover(url: String): String {
        val uri = Uri.parse(url)
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val file = File(coverFolder, fileName)

        if (file.exists()) {
            Log.d(TAG, "downloadCover: A file with the name '$fileName' exists!")
            return fileName
        } else {
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setDestinationUri(Uri.fromFile(file))
                .setMimeType("image/jpg")

            downloadManager.enqueue(request)
        }

        return fileName
    }
}