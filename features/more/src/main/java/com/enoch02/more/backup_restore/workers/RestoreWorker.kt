package com.enoch02.more.backup_restore.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.more.file_scan.BACKUP_FILE_URI_KEY
import com.enoch02.more.file_scan.PROGRESS_NOTIFICATION_ID
import com.enoch02.more.file_scan.util.createIndeterminateProgressNotification
import com.enoch02.more.file_scan.util.createProgressNotificationChannel
import com.enoch02.more.file_scan.util.makeStatusNotification
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "RestoreWorker"

//TODO: i still need to figure out how to gain access to the uri when the program is
// closed and the task is running from the background
@HiltWorker
class RestoreWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val csvManager: CSVManager
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        val backupUri = inputData.getString(BACKUP_FILE_URI_KEY)
        val notificationManager = NotificationManagerCompat.from(applicationContext)

        createProgressNotificationChannel(applicationContext)
        createIndeterminateProgressNotification(
            context = applicationContext,
            title = "Restoring Backup"
        )

        return withContext(Dispatchers.IO) {
            require(!backupUri.isNullOrBlank()) {
                val errorMessage = "Invalid Input"
                Log.e(TAG, errorMessage)
                errorMessage
            }

            try {
                Log.d(TAG, "doWork: Opening backup file for import")
                csvManager.import(Uri.parse(backupUri))

                withContext(Dispatchers.Main) {
                    notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
                    makeStatusNotification("Restore Complete!", applicationContext)
                }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "doWork: $e")
                notificationManager.cancel(PROGRESS_NOTIFICATION_ID)
                makeStatusNotification("Restore Failed!", applicationContext)

                Result.failure()
            }
        }
    }
}
