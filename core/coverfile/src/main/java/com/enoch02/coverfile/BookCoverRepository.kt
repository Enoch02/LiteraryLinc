package com.enoch02.coverfile

import android.content.Context
import android.net.Uri
import android.util.Log
import com.enoch02.util.getFileFromUri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
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
                Compressor.compress(context = context, imageFile = file) {
                    default()
                    destination(File(coverFolder.path, file.name))
                }
            }
            .onFailure {
                Log.e(TAG, "copyCover: ${it.message}")
            }

        return coverFile.getOrThrow().name
    }

    fun deleteAllCovers(scope: CoroutineScope) {
        scope.launch {
            val files = coverFolder.listFiles()?.toList()

            files?.forEach { file ->
                file.delete()
                Log.d(TAG, "${file.name} deleted!")
            }
        }
    }

    fun deleteOneCover(scope: CoroutineScope, coverName: String) {
        scope.launch {
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
    }
}