package com.enoch02.reader

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.existsAsFile
import com.enoch02.reader.util.generateThumbnail
import com.enoch02.reader.util.listDocsInDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReaderViewModel"

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    private val bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    var contentState by mutableStateOf(ContentState.Loading)
    var documentDirectory: Uri? by mutableStateOf(null)
    var documents = documentDao.getDocumentsAscending()
    val covers = bookCoverRepository.latestCoverPath

    private var loadDocumentJob: Job? = null
    private var currentCoverLoadingJob: Job? = null
    var isRefreshing by mutableStateOf(false)

    fun refreshing(context: Context) {
        isRefreshing = true
        loadDocuments(context)
    }

    /**
     * Load documents from the selected directory. Also check if new documents have
     * been added or removed from the directory to update the local database.
     * */
    fun loadDocuments(context: Context) {
        Log.e(TAG, "loadDocuments: Loading documents")
        if (loadDocumentJob == null) {
            loadDocumentJob = viewModelScope.launch(Dispatchers.IO) {
                compareLists(context)
                    .onSuccess {
                        isRefreshing = false
                    }
                    .onFailure {
                        Log.e(TAG, "loadContent", it)
                    }
            }

            loadDocumentJob = null
        }
    }


    private fun getNewCovers(context: Context) {
        if (currentCoverLoadingJob == null) {
            val coversSnapshot = bookCoverRepository.getCoverFolderSnapshot()

            currentCoverLoadingJob = viewModelScope.launch(Dispatchers.IO) {
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

                currentCoverLoadingJob = null
            }
        } else {
            Log.d(TAG, "getNewCovers: a job is still running")
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
                    documentDirectory = persistedUri.uri
                    return true
                }
            }
        }
        return false
    }

    private suspend fun compareLists(context: Context): Result<Unit> {
        return try {
            if (!isRefreshing) {
                contentState = ContentState.Loading
            }

            val db = documentDao.getDocumentsNonFlow()
            val dir = documentDirectory?.let { listDocsInDirectory(context, it) }

            if (dir != null) {
                // do nothing
                if (db.isNotEmpty() && dir.isNotEmpty() && db.size == dir.size) {
                    Log.e(TAG, "compareLists: Action not needed")
                    contentState = ContentState.Success
                    return Result.success(Unit)
                }

                // items have been deleted
                if (db.size > dir.size) {
                    Log.e(TAG, "compareLists: Remove docs")
                    removeDocsFromDb(context)
                    contentState = ContentState.Success
                }

                // items have been added
                if (dir.size > db.size) {
                    Log.e(TAG, "compareLists: Add docs")
                    addDocsToDb(context)
                    contentState = ContentState.Success
                }
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    //TODO: clean up cover files
    private suspend fun removeDocsFromDb(context: Context) {
        val documents = documentDao.getDocumentsNonFlow()

        documents.forEach { document ->
            if (!document.existsAsFile(context)) {
                documentDao.deleteDocument(document.contentUri.toString())
            }
        }
    }

    private suspend fun addDocsToDb(context: Context) {
        val dir = listDocsInDirectory(
            context = context,
            directoryUri = documentDirectory ?: Uri.EMPTY
        )

        if (dir.isNotEmpty()) {
            documentDao.insertDocuments(dir)
            getNewCovers(context)
        }
    }
}

enum class ContentState {
    Loading,
    Success,
    Empty
}
