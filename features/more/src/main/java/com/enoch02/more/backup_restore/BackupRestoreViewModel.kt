package com.enoch02.more.backup_restore

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.enoch02.resources.workers.backup_restore.BackupRestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreRepository: BackupRestoreRepository
) : ViewModel() {
    fun createCSVBackup(uri: Uri) {
        backupRestoreRepository.createBackup(uri)
    }

    fun createExcelFriendlyBackup(uri: Uri) {
        backupRestoreRepository.createBackup(uri, excelFriendly = true)
    }

    fun restoreCSVBackup(uri: Uri) {
        backupRestoreRepository.restoreBackup(uri)
    }
}