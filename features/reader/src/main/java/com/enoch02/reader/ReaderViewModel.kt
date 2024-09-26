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
import com.enoch02.reader.util.generateThumbnail
import com.enoch02.reader.util.listPdfFilesInDirectory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "ReaderViewModel"

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    private val bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    var contentState by mutableStateOf(ContentState.Loading)
    var pdfDirectory: Uri? by mutableStateOf(null)
    var documents = documentDao.getDocuments()
    val covers = bookCoverRepository.latestCoverPath

    private var currentCoverLoadingJob: Job? = null
    var isRefreshing by mutableStateOf(false)

    fun refreshing(context: Context) {
        isRefreshing = true
        loadContent(context)
    }

    fun loadContent(context: Context) {
        loadDocuments(context)
        //getNewCovers(context)
    }

    //TODO: cache the list of PDF files, load content occasionally or manually to check if there are changes
    private fun loadDocuments(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                documents.collectLatest { docs ->
                    if (docs.isEmpty()) {
                        val temp = listPdfFilesInDirectory(
                            context = context,
                            directoryUri = pdfDirectory ?: Uri.EMPTY
                        )

                        documentDao.insertDocuments(temp)
                        withContext(Dispatchers.Main) {
                            contentState = ContentState.Success
                        }

                    } else {
                        //TODO: compare db contents with books folder contents to see if an update of the db is needed
                        withContext(Dispatchers.Main) {
                            contentState = ContentState.Success
                        }
                    }

                    getNewCovers(context)
                    isRefreshing = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadContent", e)
                withContext(Dispatchers.Main) {
                    contentState = ContentState.Error
                }
            }
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
