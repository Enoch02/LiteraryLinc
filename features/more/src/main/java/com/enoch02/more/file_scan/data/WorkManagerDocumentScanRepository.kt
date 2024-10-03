package com.enoch02.more.file_scan.data

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.enoch02.more.file_scan.COVER_SCAN_WORKER_ID
import com.enoch02.more.file_scan.FILE_SCAN_WORKER_ID
import com.enoch02.more.file_scan.workers.CoverScanWorker
import com.enoch02.more.file_scan.workers.FileScanWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class WorkManagerDocumentScanRepository(
    private val workManager: WorkManager
) : DocumentScanRepository {
    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)

    override fun scanFiles() {
        val scanFiles = OneTimeWorkRequestBuilder<FileScanWorker>()
            .setId(FILE_SCAN_WORKER_ID)
            .build()
        val scanCovers = OneTimeWorkRequestBuilder<CoverScanWorker>()
            .setId(COVER_SCAN_WORKER_ID)
            .build()
        var continuation = workManager.beginUniqueWork(
            FILE_SCAN_WORKER_ID.toString(),
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            scanFiles
        )

        continuation = continuation.then(scanCovers)
        continuation.enqueue()
    }

    override fun rescanCovers() {
        val scanCovers = OneTimeWorkRequestBuilder<CoverScanWorker>()
            .setId(COVER_SCAN_WORKER_ID)
            .build()

        workManager.enqueueUniqueWork(
            COVER_SCAN_WORKER_ID.toString(),
            ExistingWorkPolicy.KEEP,
            scanCovers
        )
    }

    override fun cancelWork() {
        workManager.cancelWorkById(FILE_SCAN_WORKER_ID)
        workManager.cancelWorkById(COVER_SCAN_WORKER_ID)
    }
}