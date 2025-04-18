package com.enoch02.resources.workers.file_scan

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.enoch02.resources.workers.COVER_SCAN_WORKER_ID
import com.enoch02.resources.workers.COVER_SCAN_WORKER_KEY
import com.enoch02.resources.workers.FILE_SCAN_WORKER_ID
import com.enoch02.resources.workers.FILE_SCAN_WORKER_KEY
import com.enoch02.resources.workers.SAVED_WORK_IDS_KEY
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkManagerDocumentScanRepository(
    private val context: Context,
    private val workManager: WorkManager
) : DocumentScanRepository {
    override fun getIds(): Pair<UUID, UUID> {
        val sharedPreferences =
            context.getSharedPreferences(SAVED_WORK_IDS_KEY, Context.MODE_PRIVATE)
        val fileScanWorkId =
            sharedPreferences.getString(FILE_SCAN_WORKER_KEY, null)?.let { UUID.fromString(it) }
        val coverScanWorkId =
            sharedPreferences.getString(COVER_SCAN_WORKER_KEY, null)?.let { UUID.fromString(it) }

        return Pair(fileScanWorkId ?: FILE_SCAN_WORKER_ID, coverScanWorkId ?: COVER_SCAN_WORKER_ID)
    }

    override fun periodicBackgroundScan(frequency: Int) {
        val scanFiles = PeriodicWorkRequestBuilder<PeriodicFileScanWorker>(
            frequency.toLong(), TimeUnit.HOURS
        )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodicFileScan",
            ExistingPeriodicWorkPolicy.UPDATE,
            scanFiles
        )
    }

    override fun scanFiles() {
        val scanFiles = OneTimeWorkRequestBuilder<FileScanWorker>()
            .setId(FILE_SCAN_WORKER_ID)
            .build()
        val scanCovers = OneTimeWorkRequestBuilder<CoverScanWorker>()
            .setId(COVER_SCAN_WORKER_ID)
            .build()
        var continuation = workManager.beginUniqueWork(
            FILE_SCAN_WORKER_ID.toString(),
            ExistingWorkPolicy.REPLACE,
            scanFiles
        )

        storeWorkIds(context, FILE_SCAN_WORKER_ID, COVER_SCAN_WORKER_ID)

        continuation = continuation.then(scanCovers)
        continuation.enqueue()
    }

    override fun rescanCovers() {
        val scanCovers = OneTimeWorkRequestBuilder<CoverScanWorker>()
            .setId(COVER_SCAN_WORKER_ID)
            .build()

        storeWorkIds(context, FILE_SCAN_WORKER_ID, COVER_SCAN_WORKER_ID)

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

    override fun cancelPeriodicScan() {
        workManager.cancelUniqueWork("periodicFileScan")
    }


    private fun storeWorkIds(context: Context, fileScanWorkId: UUID, coverScanWorkId: UUID) {
        val sharedPreferences =
            context.getSharedPreferences(SAVED_WORK_IDS_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(FILE_SCAN_WORKER_KEY, fileScanWorkId.toString())
            putString(COVER_SCAN_WORKER_KEY, coverScanWorkId.toString())
            apply() // Save changes
        }
    }

    override fun clearStoredFileScanId() {
        val sharedPreferences =
            context.getSharedPreferences(SAVED_WORK_IDS_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(FILE_SCAN_WORKER_KEY)
            apply()
        }
    }

    override fun clearStoredCoverScanId() {
        val sharedPreferences =
            context.getSharedPreferences(SAVED_WORK_IDS_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(COVER_SCAN_WORKER_KEY)
            apply()
        }
    }
}