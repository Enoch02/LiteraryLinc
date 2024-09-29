package com.enoch02.more.file_scan

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Document
import com.enoch02.database.model.existsAsFile
import com.enoch02.more.file_scan.util.generateThumbnail
import com.enoch02.more.file_scan.util.listDocsInDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

const val TAG = "FileScan"

@HiltViewModel
class FileScanViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    private val bookCoverRepository: BookCoverRepository
) : ViewModel() {
    var contentState by mutableStateOf(ContentState.NotLoading)
    var documentDirectory: Uri? by mutableStateOf(null)
    var totalDocuments by mutableIntStateOf(0)

    private var loadDocumentJob: Job? = null

    /**
     * Load documents from the selected directory. Also check if new documents have
     * been added or removed from the directory to update the local database.
     * */
    fun loadDocuments(context: Context) {
        if (loadDocumentJob == null) {
            loadDocumentJob = viewModelScope.launch(Dispatchers.IO) {
                compareLists(context)
                    .onSuccess {

                    }
                    .onFailure {
                        Log.e(TAG, "loadContent", it)
                    }
            }

            loadDocumentJob = null
        }
    }


    private suspend fun getNewCovers(context: Context) {
        val coversSnapshot = bookCoverRepository.getCoverFolderSnapshot()

        documentDao.getDocumentsNonFlow().forEach { document ->
            if (!coversSnapshot.containsKey(document.cover)) {
                val bitmap = document.contentUri?.let { it1 ->
                    generateThumbnail(
                        context,
                        it1
                    )
                }
                if (bitmap != null) {
                    bookCoverRepository.saveCoverFromBitmap(
                        bitmap = bitmap,
                        name = document.id
                    )
                        .onSuccess { name ->
                            documentDao.updateDocument(document.copy(cover = name))
                            Log.d(TAG, "getCovers: cover created for ${document.name}")
                        }
                } else {
                    Log.d(TAG, "getCovers: cover not created for ${document.name}")
                }
            }
        }
    }

    fun savePickedDirectoryUri(context: Context, uri: Uri) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("directory_uri", uri.toString())
            apply()
        }
    }

    fun isDirectoryPickedBefore(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("directory_uri", null)
        totalDocuments = sharedPreferences.getInt("document_count", 0)

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

    private fun saveFoundDocsCount(context: Context, size: Int) {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("document_count", size)
            apply()
        }

        totalDocuments = size
    }

    private suspend fun compareLists(context: Context): Result<Unit> {
        return try {
            contentState = ContentState.Loading

            val db = documentDao.getDocumentsNonFlow()
            val dir = documentDirectory?.let { listDocsInDirectory(context, it) }

            if (dir != null) {
                saveFoundDocsCount(context, dir.size)

                // do nothing
                if (db.isNotEmpty() && dir.isNotEmpty() && db.size == dir.size) {
                    contentState = ContentState.NotLoading
                    return Result.success(Unit)
                }

                // items have been deleted
                if (db.size > dir.size) {
                    removeDocsFromDb(context)
                    contentState = ContentState.NotLoading
                }

                // items have been added
                if (dir.size > db.size) {
                    addDocsToDb(context, dir)
                    contentState = ContentState.NotLoading
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //TODO: clean up cover files
    //TODO: show how many docs are removed in the UI
    private suspend fun removeDocsFromDb(context: Context) {
        val documents = documentDao.getDocumentsNonFlow()

        documents.forEach { document ->
            if (!document.existsAsFile(context)) {
                documentDao.deleteDocument(document.contentUri.toString())
            }
        }
    }

    private suspend fun addDocsToDb(context: Context, dir: List<Document>) {
        documentDao.insertDocuments(dir)
        getNewCovers(context)

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Documents added", Toast.LENGTH_SHORT).show()
        }
    }

    fun reloadCovers(context: Context) {
        contentState = ContentState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            getNewCovers(context)

            withContext(Dispatchers.Main) {
                contentState = ContentState.NotLoading
            }
        }
    }
}

enum class ContentState {
    NotLoading,
    Loading
}