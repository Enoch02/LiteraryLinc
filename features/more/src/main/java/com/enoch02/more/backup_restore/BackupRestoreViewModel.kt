package com.enoch02.more.backup_restore

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.export_and_import.csv.CSVManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(private val csvManager: CSVManager) : ViewModel() {

    fun createCSVBackup(uri: Uri) {
        viewModelScope.launch {
            csvManager.export(uri)
        }
    }

    fun restoreCSVBackup(uri: Uri) {
        viewModelScope.launch {
            csvManager.import(uri)
        }
    }
}