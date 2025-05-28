package com.enoch02.bookdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookDao: BookDao,
    documentDao: DocumentDao,
    bookCoverRepository: BookCoverRepository
) : ViewModel() {
    private val covers = bookCoverRepository.latestCoverPath
    val documents = documentDao.getDocuments()
        .map { documents ->
            documents.sortedBy { document ->
                document.name
            }
        }

    var coverPath: String? by mutableStateOf(null)

    fun getBookWith(id: Int): Flow<Book?> = bookDao.getBookByIdFlow(id)

    suspend fun getCover(coverName: String?) {
        coverPath = covers.first()[coverName]
    }

    fun getCovers() = covers

    fun deleteBook(id: Int) = viewModelScope.launch(Dispatchers.IO) { bookDao.deleteBook(id) }

    fun unlinkDocumentFromBook(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.updateBook(book.copy(documentMd5 = null))
        }
    }

    fun linkDocumentToBook(book: Book, documentMd5: String) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.updateBook(book.copy(documentMd5 = documentMd5))
        }
    }

    fun updateBookStatus(book: Book, newStatus: Book.Companion.BookStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.updateBook(book.copy(status = newStatus.strName))
        }
    }

    fun updateBookRating(book: Book, newRating: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.updateBook(book.copy(personalRating = newRating))
        }
    }
}