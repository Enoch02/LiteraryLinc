package com.enoch02.more.backup_restore.data

import android.net.Uri

interface BackupRestoreRepository {
    fun createBackup(backupUri: Uri, excelFriendly: Boolean = false)

    fun restoreBackup(backupUri: Uri)
}