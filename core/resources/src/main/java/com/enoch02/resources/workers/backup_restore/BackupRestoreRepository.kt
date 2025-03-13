package com.enoch02.resources.workers.backup_restore

import android.net.Uri

interface BackupRestoreRepository {
    fun createBackup(backupUri: Uri, excelFriendly: Boolean = false)

    fun restoreBackup(backupUri: Uri)
}