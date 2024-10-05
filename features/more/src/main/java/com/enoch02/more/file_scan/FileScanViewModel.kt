package com.enoch02.more.file_scan

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
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

    private var collectCoverJob: Job? = null
    private var collectFileJob: Job? = null
    var fileScanWorkInfo by mutableStateOf<WorkInfo?>(null)
    var coverScanWorkInfo by mutableStateOf<WorkInfo?>(null)

    init {
        collectWorks()
    }

    fun collectWorks() {
        val ids = documentScanRepository.getIds()
        val fileScanWorkId = ids.first
        val coverScanWorkId = ids.second

        collectFileScanWork(fileScanWorkId)
        collectCoverScanWork(coverScanWorkId)
    }

    private fun collectFileScanWork(id: UUID) {
        if (collectFileJob == null) {
            collectFileJob = viewModelScope.launch {
                workManager.getWorkInfoByIdFlow(id)
                    .collectLatest {
                        fileScanWorkInfo = it
                    }

                collectFileJob = null
            }
        } else {
            Log.d(TAG, "collectFileScanWork: A job is running!")
        }
    }

    private fun collectCoverScanWork(id: UUID) {
        if (collectCoverJob == null) {
            collectCoverJob = viewModelScope.launch {
                workManager.getWorkInfoByIdFlow(id)
                    .collectLatest {
                        coverScanWorkInfo = it
                    }

                collectCoverJob = null
            }
        } else {
            Log.d(TAG, "collectCoverScanWork: A job is running!")
        }
    }

    fun clearStoredFileScanId() {
        documentScanRepository.clearStoredFileScanId()
    }

    fun clearStoredCoverScanId() {
        documentScanRepository.clearStoredCoverScanId()
    }

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
}