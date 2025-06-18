package com.enoch02.resources.workers.backup_restore

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.resources.createIndeterminateProgressNotification
import com.enoch02.resources.createProgressNotificationChannel
import com.enoch02.resources.makeStatusNotification
import com.enoch02.resources.workers.BACKUP_FILE_URI_KEY
import com.enoch02.resources.workers.RESTORE_NOTIFICATION_ID
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "RestoreWorker"

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
            title = "Restoring Backup",
            id = RESTORE_NOTIFICATION_ID
        )

        return withContext(Dispatchers.IO) {
            require(!backupUri.isNullOrBlank()) {
                val errorMessage = "Invalid Input"
                Log.e(TAG, errorMessage)
                errorMessage
            }

            try {
                Log.d(TAG, "doWork: Opening backup file for import")
                csvManager.import(uri = backupUri.toUri())
                    .onSuccess {
                        withContext(Dispatchers.Main) {
                            notificationManager.cancel(RESTORE_NOTIFICATION_ID)
                            makeStatusNotification(
                                "Restore Complete!",
                                applicationContext,
                                RESTORE_NOTIFICATION_ID
                            )
                        }
                    }
                    .onFailure {
                        notificationManager.cancel(RESTORE_NOTIFICATION_ID)
                        makeStatusNotification(
                            "Restore Failed: ${it.message}",
                            applicationContext,
                            RESTORE_NOTIFICATION_ID
                        )
                    }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "doWork: $e")
                Result.failure()
            }
        }
    }
}