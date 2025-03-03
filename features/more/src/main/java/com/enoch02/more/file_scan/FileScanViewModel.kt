package com.enoch02.more.file_scan

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.enoch02.database.dao.DocumentDao
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject

const val TAG = "FileScan"

@HiltViewModel
class FileScanViewModel @Inject constructor(
    private val documentScanRepository: DocumentScanRepository,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager,
    documentDao: DocumentDao
) : ViewModel() {
    var documentDirectory: Uri? by mutableStateOf(null)
    var totalDocuments = documentDao.getDocumentCount()
    var scanDirectories by mutableStateOf(emptyMap<String?, Uri>())

    private var coverScanCollectionJob: Job? = null
    private var fileScanWorkCollectionJob: Job? = null

    val fileScanWorkInfo = MutableStateFlow<WorkInfo?>(null)
    val coverScanWorkInfo = MutableStateFlow<WorkInfo?>(null)

    fun collectWorks() {
        val ids = documentScanRepository.getIds()
        val fileScanWorkId = ids.first
        val coverScanWorkId = ids.second

        collectFileScanWork(fileScanWorkId)
        collectCoverScanWork(coverScanWorkId)
    }

    private fun collectFileScanWork(fileScanWorkId: UUID) {
        if (fileScanWorkCollectionJob == null) {
            fileScanWorkCollectionJob = workManager.getWorkInfoByIdFlow(fileScanWorkId)
                .onEach { fileScanWorkInfo.value = it }
                .catch { exception ->
                    Log.e(TAG, "Error collecting file scan work info", exception)
                    //TODO: show error msg?
                }
                .launchIn(viewModelScope)
        } else {
            Log.d(TAG, "collectFileScanWork: A job is running!")
        }
    }

    private fun collectCoverScanWork(coverScanWorkId: UUID) {
        if (coverScanCollectionJob == null) {
            coverScanCollectionJob = viewModelScope.launch {
                workManager.getWorkInfoByIdFlow(coverScanWorkId)
                    .onEach { coverScanWorkInfo.value = it }
                    .catch { exception ->
                        Log.e(TAG, "Error collecting cover scan work info", exception)
                    }
                    .launchIn(viewModelScope)
            }
        } else {
            Log.d(TAG, "collectCoverScanWork: A job is running!")
        }
    }

    fun isScanningFiles(fileScanInfo: WorkInfo?): Boolean {
        if (fileScanInfo == null || fileScanInfo.state.isFinished) {
            if (fileScanInfo?.state?.isFinished == true) {
                clearStoredFileScanId()
                fileScanWorkCollectionJob?.cancel()
                fileScanWorkCollectionJob = null
            }
            return false
        }
        return true
    }

    fun isScanningCovers(coverScanInfo: WorkInfo?): Boolean {
        if (coverScanInfo == null || coverScanInfo.state.isFinished) {
            if (coverScanInfo?.state?.isFinished == true) {
                clearStoredCoverScanId()
                coverScanCollectionJob?.cancel()
                coverScanCollectionJob = null
            }
            return false
        }
        return true
    }

    private fun clearStoredFileScanId() {
        documentScanRepository.clearStoredFileScanId()
    }

    private fun clearStoredCoverScanId() {
        documentScanRepository.clearStoredCoverScanId()
    }

    fun loadDocuments(context: Context, isScanningFiles: Boolean, isScanningCovers: Boolean) {
        if (documentDirectory == null) {
            Toast.makeText(
                context,
                "Select app directory",
                Toast.LENGTH_SHORT
            )
                .show()
        } else if (isScanningFiles || isScanningCovers) {
            Toast.makeText(
                context,
                "A scan is in progress",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            documentScanRepository.scanFiles()
        }
    }

    fun rescanCovers(context: Context, isScanningFiles: Boolean, isScanningCovers: Boolean) {
        if (documentDirectory == null || scanDirectories.isEmpty()) {
            Toast.makeText(
                context,
                "Select app directory",
                Toast.LENGTH_SHORT
            )
                .show()
        } else if (isScanningFiles || isScanningCovers) {
            Toast.makeText(
                context,
                "A scan is in progress",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            documentScanRepository.rescanCovers()
        }
    }

    fun togglePeriodicScans(frequency: Int) {
        documentScanRepository.periodicBackgroundScan(frequency)
    }

    fun cancelPeriodicScan() {
        documentScanRepository.cancelPeriodicScan()
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

    /**
     * Retrieves the list of directories the user has granted persistent access to.
     *
     * @param context The context to access the content resolver.
     * @return A list of URIs representing the directories with persistent access.
     */
    fun getPersistedDirectories(context: Context) {
        fun extractFolderName(uri: Uri): String? {
            // Get the last path segment (after "tree/")
            val lastSegment = uri.lastPathSegment ?: return null
            // Decode the URI-encoded string
            val decodedSegment = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8.name())
            // Split the decoded string by ":" to get the actual folder path
            val splitSegment = decodedSegment.split(":")

            // Return the last part of the folder path (the folder name)
            return splitSegment.getOrNull(1)?.substringAfterLast('/')
        }


        val persistedUriPermissions = context.contentResolver.persistedUriPermissions
        val uris = persistedUriPermissions.map { it.uri }

        scanDirectories = uris.associateBy { uri ->
            extractFolderName(uri)
        }
    }

    /**
     * Removes persistent access to a folder or document.
     *
     * @param context The context to access the content resolver.
     * @param uri The Uri of the folder or document for which to revoke access.
     */
    fun removePersistedFolderAccess(context: Context, uri: Uri) {
        try {
            // Release both read and write URI permissions
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            // Handle exception if the URI permission does not exist or was already released
        }
    }

    fun switchPreference(key: SettingsRepository.BooleanPreferenceType, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }

    fun switchPreference(key: SettingsRepository.IntPreferenceType, newValue: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: SettingsRepository.IntPreferenceType): Flow<Int> {
        return settingsRepository.getPreference(key)
    }
}