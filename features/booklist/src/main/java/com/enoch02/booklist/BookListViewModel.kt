package com.enoch02.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository,
    private val documentDao: DocumentDao
) : ViewModel() {
    private val books = bookDao.getBooks()
    private val covers = bookCoverRepository.latestCoverPath
    val documents = documentDao.getDocuments()
        .map { documents ->
            documents.sortedByDescending { document ->
                document.lastRead
            }
        }

    fun getBooks(filter: Int, sorting: Sorting, status: StatusFilter): Flow<List<Book>> {
        val filteredBooks = filterBooks(filter)
        val sortedBooks = sortBooks(sorting = sorting, filteredBooks = filteredBooks)

        return filterStatus(status = status, sortedBooks = sortedBooks)
    }

    private fun filterBooks(filter: Int): Flow<List<Book>> {
        return if (filter == 0) {
            books
        } else {
            books.map { books ->
                books.filter { book ->
                    book.type == Book.types[filter]
                }
            }
        }
    }

    private fun sortBooks(sorting: Sorting, filteredBooks: Flow<List<Book>>): Flow<List<Book>> {
        return when (sorting) {
            Sorting.ALPHABETICAL -> {
                filteredBooks.map { books -> books.sortedBy { it.title } }
            }

            Sorting.ALPHABETICAL_REVERSE -> {
                filteredBooks.map { books -> books.sortedByDescending { it.title } }
            }

            Sorting.DATE_STARTED -> {
                filteredBooks.map { books -> books.sortedBy { it.dateStarted } }
            }

            Sorting.DATE_STARTED_REVERSE -> {
                filteredBooks.map { books -> books.sortedByDescending { it.dateStarted } }
            }
        }
    }

    private fun filterStatus(
        status: StatusFilter,
        sortedBooks: Flow<List<Book>>
    ): Flow<List<Book>> {
        return if (status == StatusFilter.ALL) {
            sortedBooks
        } else {
            sortedBooks.map { books ->
                books.filter { book -> book.status == status.stringify() }
            }
        }
    }

    fun getCovers() = covers

    fun deleteBook(id: Int) {
        viewModelScope.launch(Dispatchers.IO) { bookDao.deleteBook(id) }
    }

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
}