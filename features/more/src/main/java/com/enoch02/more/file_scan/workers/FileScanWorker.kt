package com.enoch02.more.file_scan.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Document
import com.enoch02.database.model.existsAsFile
import com.enoch02.more.file_scan.APP_PREFS_KEY
import com.enoch02.more.file_scan.DOCUMENT_COUNT_KEY
import com.enoch02.more.file_scan.DOCUMENT_DIR_KEY
import com.enoch02.more.file_scan.util.listDocsInDirectory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "FileScanWorker"

@HiltWorker
class FileScanWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val documentDao: DocumentDao
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val db = documentDao.getDocumentsNonFlow()
            val dir = getDirUri()?.let { listDocsInDirectory(applicationContext, it) }

            if (dir != null) {
                saveFoundDocsCount(applicationContext, dir.size)

                // do nothing
                if (db.isNotEmpty() && dir.isNotEmpty() && db.size == dir.size) {
                    Log.d(TAG, "doWork: Doing nothing")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "No new files have been found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // items have been deleted
                if (db.size > dir.size) {
                    removeDocsFromDb(applicationContext)
                }

                // items have been added
                if (dir.size > db.size) {
                    addDocsToDb(applicationContext, dir)
                }
            } else {
                //TODO: send a notification when the scan fails???
                Result.failure()
            }

            Result.success()
        }
    }

    private fun getDirUri(): Uri? {
        val sharedPreferences =
            applicationContext.getSharedPreferences(APP_PREFS_KEY, Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(DOCUMENT_DIR_KEY, null)

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val persistedUris = applicationContext.contentResolver.persistedUriPermissions
            for (persistedUri in persistedUris) {
                if (persistedUri.uri == uri) {
                    return uri
                }
            }
        }

        return null
    }

    private fun saveFoundDocsCount(context: Context, size: Int) {
        val sharedPreferences = context.getSharedPreferences(APP_PREFS_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt(DOCUMENT_COUNT_KEY, size)
            apply()
        }
    }

    //TODO: clean up cover files
    //TODO: show how many docs are removed in the UI
    private suspend fun removeDocsFromDb(context: Context) {
        val documents = documentDao.getDocumentsNonFlow()
        var removedCount = 0

        documents.forEach { document ->
            if (!document.existsAsFile(context)) {
                documentDao.deleteDocument(document.contentUri.toString())
                removedCount += 1
            }
        }

        withContext(Dispatchers.Main) {
            makeStatusNotification(message = "$removedCount documents added removed", context)
        }
    }

    private suspend fun addDocsToDb(context: Context, dir: List<Document>) {
        documentDao.insertDocuments(dir)

        withContext(Dispatchers.Main) {
            makeStatusNotification(message = "${dir.size} new documents added", context)
        }
    }
}