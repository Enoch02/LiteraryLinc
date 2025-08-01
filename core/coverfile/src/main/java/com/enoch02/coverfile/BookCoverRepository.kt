package com.enoch02.coverfile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.enoch02.util.getFileFromUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
            emit(getCoverFolderSnapshot())
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

    suspend fun copyCoverFrom(bitmap: Bitmap?, name: String): String {
        val coverFile = File(coverFolder, "$name.jpg")

        return withContext(Dispatchers.IO) {
            try {
                FileOutputStream(coverFile).use {
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                bitmap?.recycle()
                coverFile.name
            } catch (e: Exception) {
                ""
            }
        }
    }

    suspend fun saveCoverFromBitmap(bitmap: Bitmap, name: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(coverFolder, "$name.png")
                val fileOutputStream = FileOutputStream(file)

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.close()

                Result.success(file.name)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getCoverFolderSnapshot(): Map<String, String?> {
        val latestPathMap = mutableMapOf<String, String?>()
        coverFolder.listFiles()?.forEach {
            latestPathMap[it.name] = it.absolutePath
        }

        return latestPathMap.toMap()
    }

    suspend fun deleteCover(name: String): Result<Unit> {
        try {
            val fileToDelete = latestCoverPath.first()
                .filterKeys { it.contains(name) }
                .firstNotNullOf { it.value }
            val file = File(fileToDelete)

            return if (file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Could not delete ${file.absolutePath}"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
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