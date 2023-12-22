package com.enoch02.coverfile

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
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val TAG = "COVER_REPO"

class BookCoverRepository(private val context: Context) {
    private val coverFolder = File(context.filesDir.path, "covers/")

    init {
        if (!coverFolder.exists()) {
            coverFolder.mkdir()
        }
    }

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

    suspend fun downloadCover(coverUrl: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val fileName = coverUrl.substring(coverUrl.lastIndexOf('/') + 1)
            val destinationFile = File(coverFolder.path + "/$fileName")
            val urlObj = URL(coverUrl)
            val connection = urlObj.openConnection() as HttpURLConnection

            try {
                val inputStream = connection.inputStream
                val fileOutputStream = FileOutputStream(destinationFile)

                val buffer = ByteArray(1024)
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()

                return@withContext Result.failure(e)
            } finally {
                connection.disconnect()
            }

            return@withContext Result.success(fileName)
        }
    }
}