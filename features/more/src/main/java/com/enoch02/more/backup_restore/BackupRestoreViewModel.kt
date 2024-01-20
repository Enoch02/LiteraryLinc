package com.enoch02.more.backup_restore

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.export_and_import.csv.CSVManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(private val csvManager: CSVManager) : ViewModel() {
    val showErrorDialog = mutableStateOf(false)
    val errorDialogMessage = mutableStateOf("")

    fun createCSVBackup(uri: Uri) {
        viewModelScope.launch {
            csvManager.export(uri)
        }
    }

    fun restoreCSVBackup(uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            csvManager.import(uri)
                .onSuccess {
                    onSuccess()
                }
                .onFailure { e ->
                    showErrorDialog(e.stackTraceToString())
                }
        }
    }

    private fun showErrorDialog(message: String) {
        showErrorDialog.value = true
        errorDialogMessage.value = "Invalid Input Encountered $message"
    }

    fun closeErrorDialog() {
        showErrorDialog.value = false
        errorDialogMessage.value = ""
    }
}