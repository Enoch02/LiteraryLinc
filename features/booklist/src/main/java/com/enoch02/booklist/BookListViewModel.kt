package com.enoch02.booklist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) : ViewModel() {
    private val books = bookDao.getBooks()
    private val covers = bookCoverRepository.latestCoverPath
    val pBooks = mutableStateListOf<Book>()

    /*fun getBooks(filter: Int, sorting: Sorting): Flow<List<Book>> {
        val temp = if (filter == 0) books else
            books.map { books ->
                books.filter { book ->
                    book.type == Book.types[filter]
                }
            }

        return when (sorting) {
            Sorting.ALPHABETICAL -> {
                temp.map { books -> books.sortedBy { it.title } }
            }

            Sorting.ALPHABETICAL_REVERSE -> {
                temp.map { books -> books.sortedByDescending { it.title } }
            }

            Sorting.DATE_STARTED -> {
                temp.map { books -> books.sortedBy { it.dateStarted } }
            }

            Sorting.DATE_STARTED_REVERSE -> {
                temp.map { books -> books.sortedByDescending { it.dateStarted } }
            }
        }
    }*/

    fun getBooks(filter: Int, sorting: Sorting, status: StatusFilter): Flow<List<Book>> {
        val filteredBooks = filterBooks(filter)
        val sortedBooks = sortBooks(sorting=sorting, filteredBooks = filteredBooks)

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

    private fun filterStatus(status: StatusFilter, sortedBooks: Flow<List<Book>>): Flow<List<Book>> {
        return if (status == StatusFilter.ALL){
            sortedBooks
        } else {
            sortedBooks.map { books ->
                books.filter { book -> book.status == status.stringify() }
            }
        }
    }

    fun getCovers() = covers

    fun deleteBook(id: Int) {
        viewModelScope.launch { bookDao.deleteBook(id) }
    }

    fun incrementBook(id: Int) {
        viewModelScope.launch {
            var book = bookDao.getBookById(id)

            book = book.copy(pagesRead = book.pagesRead + 1)
            bookDao.updateBook(book)
        }
    }
}