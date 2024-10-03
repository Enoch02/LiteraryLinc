package com.enoch02.more.file_scan

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.enoch02.more.file_scan.data.DocumentScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val TAG = "FileScan"

@HiltViewModel
class FileScanViewModel @Inject constructor(
    private val documentScanRepository: DocumentScanRepository,
    private val workManager: WorkManager
) : ViewModel() {
    var contentState by mutableStateOf(ContentState.NotLoading)
    var documentDirectory: Uri? by mutableStateOf(null)
    var totalDocuments by mutableIntStateOf(0)

    val fileScanWorkInfoFlow = workManager.getWorkInfoByIdFlow(FILE_SCAN_WORKER_ID)
    val coverScanWorkInfoFlow = workManager.getWorkInfoByIdFlow(COVER_SCAN_WORKER_ID)

    fun loadDocuments() {
        documentScanRepository.scanFiles()
        contentState = ContentState.Loading
    }

    fun rescanCovers() {
        documentScanRepository.rescanCovers()
        contentState = ContentState.Loading
    }

    fun savePickedDirectoryUri(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences(APP_PREFS_KEY, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(DOCUMENT_DIR_KEY, uri.toString())
            apply()
        }
    }

    fun isDirectoryPickedBefore(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences(APP_PREFS_KEY, Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(DOCUMENT_DIR_KEY, null)
        totalDocuments = sharedPreferences.getInt(DOCUMENT_COUNT_KEY, 0)

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val persistedUris = context.contentResolver.persistedUriPermissions
            for (persistedUri in persistedUris) {
                if (persistedUri.uri == uri) {
                    documentDirectory = persistedUri.uri
                    return true
                }
            }
        }

        return false
    }
}

enum class ContentState {
    NotLoading,
    Loading,
    Error
}