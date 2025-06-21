package com.enoch02.resources.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.resources.workers.backup_restore.BackupWorker
import com.enoch02.resources.workers.backup_restore.RestoreWorker
import com.enoch02.resources.workers.file_scan.CoverScanWorker
import com.enoch02.resources.workers.file_scan.FileScanWorker
import com.enoch02.resources.workers.file_scan.PeriodicFileScanWorker

class WorkerFactory(
    private val documentDao: DocumentDao,
    private val bookCoverRepository: BookCoverRepository,
    private val csvManager: CSVManager
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): CoroutineWorker? {
        return when (workerClassName) {
            FileScanWorker::class.java.name -> {
                FileScanWorker(appContext, workerParameters, documentDao)
            }

            CoverScanWorker::class.java.name -> {
                CoverScanWorker(appContext, workerParameters, bookCoverRepository, documentDao)
            }

            BackupWorker::class.java.name -> {
                BackupWorker(appContext, workerParameters, csvManager)
            }

            RestoreWorker::class.java.name -> {
                RestoreWorker(appContext, workerParameters, csvManager)
            }

            PeriodicFileScanWorker::class.java.name -> {
                PeriodicFileScanWorker(appContext, workerParameters, documentDao)
            }

            else -> {
                null
            }
        }
    }
}