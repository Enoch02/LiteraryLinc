package com.enoch02.resources.workers.file_scan

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.enoch02.database.dao.DocumentDao
import com.enoch02.resources.createIndeterminateProgressNotification
import com.enoch02.resources.createProgressNotificationChannel
import com.enoch02.resources.workers.PERIODIC_FILE_SCAN_NOTIFICATION_ID
import com.enoch02.resources.workers.file_scan.util.addDocumentsToDb
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class PeriodicFileScanWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val documentDao: DocumentDao
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            fileScan()

            val coverScanRequest = OneTimeWorkRequestBuilder<CoverScanWorker>().build()
            WorkManager.getInstance(applicationContext).enqueue(coverScanRequest)

            Result.success()
        }
    }

    private suspend fun fileScan() {
        createProgressNotificationChannel(applicationContext)
        createIndeterminateProgressNotification(
            context = applicationContext,
            title = "Loading Documents",
            id = PERIODIC_FILE_SCAN_NOTIFICATION_ID
        )

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        val db = documentDao.getDocumentsNonFlow()
        val persistedUriPermissions = applicationContext.contentResolver.persistedUriPermissions
        val uris = persistedUriPermissions.map { it.uri }
        uris.forEach { uri ->
            addDocumentsToDb(
                context = applicationContext,
                directoryUri = uri,
                scanned = db.map { document -> document.contentUri },
                dao = documentDao
            )
        }
        notificationManager.cancel(PERIODIC_FILE_SCAN_NOTIFICATION_ID)
    }
}