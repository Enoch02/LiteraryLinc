package com.enoch02.more.file_scan.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface DocumentScanRepository {
    val outputWorkInfo: Flow<WorkInfo?>

    /**
     * Load documents from the selected directory. Also check if new documents have
     * been added or removed from the directory to update the local database.
     * */
    fun scanFiles()

    /*
    * Try to rescan cover files for documents that might not have their cover
    * loaded for various reasons
    * */
    fun rescanCovers()

    fun cancelWork()
}