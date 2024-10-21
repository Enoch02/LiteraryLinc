package com.enoch02.more.backup_restore

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.export_and_import.csv.CSVManager
import com.enoch02.more.backup_restore.data.BackupRestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRestoreRepository: BackupRestoreRepository
) : ViewModel() {
    fun createCSVBackup(uri: Uri) {
        backupRestoreRepository.createBackup(uri)
    }

    fun restoreCSVBackup(uri: Uri, onSuccess: () -> Unit) {
        backupRestoreRepository.restoreBackup(uri)
    }
}