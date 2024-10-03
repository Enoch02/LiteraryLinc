package com.enoch02.more.backup_restore.data

import android.net.Uri

interface BackupRestoreRepository {
    fun createBackup(backupUri: Uri)

    fun restoreBackup(backupUri: Uri)
}