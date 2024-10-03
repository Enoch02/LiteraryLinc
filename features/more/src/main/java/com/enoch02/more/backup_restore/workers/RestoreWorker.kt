package com.enoch02.more.backup_restore.workers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.more.file_scan.BACKUP_FILE_URI_KEY
import com.enoch02.more.file_scan.workers.makeStatusNotification
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

        makeStatusNotification("Restoring Backup", applicationContext)

        return withContext(Dispatchers.IO) {
            require(!backupUri.isNullOrBlank()) {
                val errorMessage = "Invalid Input"
                Log.e(TAG, errorMessage)
                errorMessage
            }

            try {
                csvManager.import(Uri.parse(backupUri))
                withContext(Dispatchers.Main) {
                    makeStatusNotification("Restore Complete!", applicationContext)
                }

                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "doWork: $e")

                Result.failure()
            }
        }
    }
}
