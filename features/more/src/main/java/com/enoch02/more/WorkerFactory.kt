package com.enoch02.more

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.more.backup_restore.workers.BackupWorker
import com.enoch02.more.backup_restore.workers.RestoreWorker
import com.enoch02.more.file_scan.workers.CoverScanWorker
import com.enoch02.more.file_scan.workers.FileScanWorker

class WorkerFactory(
    private val documentDao: DocumentDao,
    private val bookCoverRepository: BookCoverRepository,
    private val csvManager: CSVManager
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        className: String,
        parameters: WorkerParameters
    ): CoroutineWorker? {
        return when (className) {
            FileScanWorker::class.java.name -> {
                FileScanWorker(appContext, parameters, documentDao)
            }

            CoverScanWorker::class.java.name -> {
                CoverScanWorker(appContext, parameters, bookCoverRepository, documentDao)
            }

            BackupWorker::class.java.name -> {
                BackupWorker(appContext, parameters, csvManager)
            }

            RestoreWorker::class.java.name -> {
                RestoreWorker(appContext, parameters, csvManager)
            }

            else -> {
                null
            }
        }
    }
}