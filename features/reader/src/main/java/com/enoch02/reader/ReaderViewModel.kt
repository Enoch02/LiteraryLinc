package com.enoch02.reader

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.LLDocument
import com.enoch02.database.model.ReaderSorting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReaderViewModel"

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val documentDao: DocumentDao,
    bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    val covers = bookCoverRepository.latestCoverPath

    fun isDirectoryPickedBefore(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("directory_uri", null)

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val persistedUris = context.contentResolver.persistedUriPermissions
            for (persistedUri in persistedUris) {
                if (persistedUri.uri == uri) {
                    return true
                }
            }
        }
        return false
    }

    fun getDocuments(sorting: ReaderSorting): Flow<List<LLDocument>> {
        return when (sorting) {
            ReaderSorting.NAME -> {
                documentDao.getDocumentsByName()
            }

            ReaderSorting.LAST_READ -> {
                documentDao.getDocumentsByLastRead()
            }

            ReaderSorting.SIZE -> {
                documentDao.getDocumentsBySize()
            }

            ReaderSorting.FORMAT -> {
                documentDao.getDocumentsByFormat()
            }
        }
    }

    fun updateDocumentInfo(document: LLDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.updateDocument(document)
        }
    }
}
