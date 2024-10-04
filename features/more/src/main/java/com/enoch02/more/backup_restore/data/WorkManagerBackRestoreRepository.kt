package com.enoch02.more.backup_restore.data

import android.net.Uri
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.enoch02.more.backup_restore.workers.BackupWorker
import com.enoch02.more.backup_restore.workers.RestoreWorker
import com.enoch02.more.file_scan.BACKUP_FILE_URI_KEY
import com.enoch02.more.file_scan.CREATE_BACKUP_WORKER_ID
import com.enoch02.more.file_scan.RESTORE_BACKUP_WORKER_ID

class WorkManagerBackRestoreRepository(private val workManager: WorkManager) :
    BackupRestoreRepository {
    override fun createBackup(backupUri: Uri) {
        val data = Data.Builder().putString(BACKUP_FILE_URI_KEY, backupUri.toString()).build()
        val createBackup = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(data)
            .setId(CREATE_BACKUP_WORKER_ID)
            .build()

        workManager.enqueueUniqueWork(
            CREATE_BACKUP_WORKER_ID.toString(),
            ExistingWorkPolicy.KEEP,
            createBackup
        )
    }

    override fun restoreBackup(backupUri: Uri) {
        val data = Data.Builder().putString(BACKUP_FILE_URI_KEY, backupUri.toString()).build()
        val restoreBackup = OneTimeWorkRequestBuilder<RestoreWorker>()
            .setInputData(data)
            .setId(RESTORE_BACKUP_WORKER_ID)
            .build()

        workManager.enqueueUniqueWork(
            RESTORE_BACKUP_WORKER_ID.toString(),
            ExistingWorkPolicy.KEEP,
            restoreBackup
        )
    }
}