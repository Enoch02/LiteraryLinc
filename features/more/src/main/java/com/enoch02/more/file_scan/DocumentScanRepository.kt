package com.enoch02.more.file_scan

import java.util.UUID

interface DocumentScanRepository {
    fun getIds(): Pair<UUID, UUID>

    fun periodicBackgroundScan(frequency: Int)

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

    fun cancelPeriodicScan()

    fun clearStoredFileScanId()

    fun clearStoredCoverScanId()
}