package com.enoch02.booklist

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import com.enoch02.resources.extensions.naturalCompare
import com.enoch02.resources.extensions.uniqueAdd
import com.enoch02.resources.extensions.uniqueAddAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookDao: BookDao,
    bookCoverRepository: BookCoverRepository,
) : ViewModel() {
    private val books = bookDao.getBooks()
    private val covers = bookCoverRepository.latestCoverPath

    private val _selectedBooks = mutableStateListOf<Int>()
    val selectedBooks: List<Int> = _selectedBooks

    fun getBooks(filter: Int, sorting: Sorting, status: StatusFilter): Flow<List<Book>> {
        val filteredBooks = filterBooks(filter)
        val sortedBooks = sortBooks(sorting = sorting, filteredBooks = filteredBooks)
        val final = filterStatus(status = status, sortedBooks = sortedBooks)

        return final
    }

    private fun filterBooks(filter: Int): Flow<List<Book>> {
        return if (filter == 0) {
            books
        } else {
            books.map { books ->
                books.filter { book ->
                    book.type == Book.Companion.BookType.entries[filter].strName
                }
            }
        }
    }

    private fun sortBooks(sorting: Sorting, filteredBooks: Flow<List<Book>>): Flow<List<Book>> {
        return when (sorting) {
            Sorting.ALPHABETICAL -> {
                filteredBooks.map { books ->
                    books.sortedWith(
                        Comparator { a, b ->
                            a.title.naturalCompare(b.title)
                        }
                    )
                }
            }

            Sorting.ALPHABETICAL_REVERSE -> {
                filteredBooks.map { books ->
                    books.sortedWith(
                        Comparator { a, b ->
                            a.title.naturalCompare(b.title)
                        }
                    )
                        .asReversed()
                }
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

    fun isBookSelected(id: Int): Boolean = _selectedBooks.contains(id)

    fun addToSelectedBooks(id: Int) {
        _selectedBooks.uniqueAdd(id)
    }

    fun removeFromSelectedBooks(id: Int) {
        _selectedBooks.remove(id)
    }

    fun deleteSelectedBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val iterator = _selectedBooks.iterator()

            while (iterator.hasNext()) {
                deleteBook(iterator.next())
                iterator.remove()
            }
        }
    }

    fun clearSelectedBooks() {
        _selectedBooks.clear()
    }

    fun selectAllBooks(currentType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = filterBooks(currentType).first()
            val ids = books.mapNotNull { it.id }

            _selectedBooks.uniqueAddAll(ids)
        }
    }

    fun invertSelection(currentType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val books = filterBooks(currentType).first()
            val unSelectedBooks = books
                .filterNot { book -> _selectedBooks.contains(book.id) }
                .map { it.id!! }

            _selectedBooks.clear()
            _selectedBooks.addAll(unSelectedBooks)
        }
    }

    fun searchFor(text: String, currentType: Int, statusFilter: StatusFilter): Flow<List<Book>> {
        val booksFilteredByType = filterBooks(currentType)
        val booksFilteredByStatus =
            filterStatus(status = statusFilter, sortedBooks = booksFilteredByType)

        return booksFilteredByStatus.map { documents ->
            if (text.isBlank()) {
                emptyList()
            } else {
                documents.filter {
                    it.title.contains(text, ignoreCase = true) ||
                            it.author.contains(text, ignoreCase = true)
                }
            }
        }
    }
}