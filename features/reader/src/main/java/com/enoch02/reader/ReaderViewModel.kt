package com.enoch02.reader

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.reader.models.PdfFile
import com.enoch02.reader.util.listPdfFilesInDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "ReaderViewModel"

class ReaderViewModel : ViewModel() {
    var contentState by mutableStateOf(ContentState.Loading)
    var pdfDirectory: Uri? by mutableStateOf(null)
    var pdfFiles: List<PdfFile> by mutableStateOf(emptyList())

    fun loadContent(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pdfFiles = listPdfFilesInDirectory(context, pdfDirectory ?: Uri.EMPTY)
                contentState = ContentState.Success
            } catch (e: Exception) {
                Log.e(TAG, "loadContent: Permission not granted!")
                contentState = ContentState.Error
            }
        }
    }

    fun savePickedDirectoryUri(context: Context, uri: Uri) {
        //TODO: replace with datastore
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("directory_uri", uri.toString())
            apply()
        }
    }

    fun isDirectoryPickedBefore(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("directory_uri", null)

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val persistedUris = context.contentResolver.persistedUriPermissions
            for (persistedUri in persistedUris) {
                if (persistedUri.uri == uri) {
                    pdfDirectory = persistedUri.uri
                    return true
                }
            }
        }
        return false
    }
}

enum class ContentState {
    Loading,
    Success,
    Error
}
