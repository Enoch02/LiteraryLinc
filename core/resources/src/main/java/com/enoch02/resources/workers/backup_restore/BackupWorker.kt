package com.enoch02.resources.workers.backup_restore

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.resources.makeStatusNotification
import com.enoch02.resources.workers.BACKUP_FILE_URI_KEY
import com.enoch02.resources.workers.BACKUP_NOTIFICATION_ID
import com.enoch02.resources.workers.EXCEL_FRIENDLY_KEY
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "BackupWorker"

class BackupWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted parameters: WorkerParameters,
    private val csvManager: CSVManager
) : CoroutineWorker(ctx, parameters) {
    override suspend fun doWork(): Result {
        val backupUri = inputData.getString(BACKUP_FILE_URI_KEY)
        val friendly = inputData.getBoolean(EXCEL_FRIENDLY_KEY, false)

        makeStatusNotification("Creating Backup", applicationContext, BACKUP_NOTIFICATION_ID)

        return withContext(Dispatchers.IO) {
            require(!backupUri.isNullOrBlank()) {
                val errorMessage = "Invalid Input"
                Log.e(TAG, errorMessage)
                errorMessage
            }

            try {
                val uri = Uri.parse(backupUri)

                if (friendly) {
                    csvManager.excelFriendlyExport(uri)
                } else {
                    csvManager.export(uri)
                }

                withContext(Dispatchers.Main) {
                    makeStatusNotification(
                        "Backup Complete!",
                        applicationContext,
                        BACKUP_NOTIFICATION_ID
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