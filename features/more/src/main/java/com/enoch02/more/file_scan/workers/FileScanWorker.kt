package com.enoch02.more.file_scan.workers

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.LLDocument
import com.enoch02.more.file_scan.PROGRESS_NOTIFICATION_ID
import com.enoch02.more.file_scan.util.createIndeterminateProgressNotification
import com.enoch02.more.file_scan.util.createProgressNotificationChannel
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
            createProgressNotificationChannel(applicationContext)
            createIndeterminateProgressNotification(
                context = applicationContext,
                title = "Loading Documents"
            )

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            val db = documentDao.getDocumentsNonFlow()
            val persistedUriPermissions = applicationContext.contentResolver.persistedUriPermissions
            val uris = persistedUriPermissions.map { it.uri }
            val dir = uris.map {
                listDocsInDirectory(
                    context = applicationContext,
                    directoryUri = it,
                    scanned = db.map { document -> document.contentUri }
                )
            }.flatten()

            addDocsToDb(dir)
            cleanUp()
            notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
            Result.success()
        }
    }

    private suspend fun addDocsToDb(dir: List<LLDocument>) {
        documentDao.insertDocuments(dir)
    }

    //TODO: remove unused covers here
    private suspend fun cleanUp() {

    }
}
