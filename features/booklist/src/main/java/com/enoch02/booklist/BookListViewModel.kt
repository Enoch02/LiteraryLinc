package com.enoch02.booklist

import androidx.lifecycle.ViewModel
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) : ViewModel() {
    private val books = bookDao.getBooks()
    private val covers = bookCoverRepository.latestCoverPath

    fun getBooks(filter: Int): Flow<List<Book>> {
        if (filter == 0) {
            return books
        }

        return books.map { books -> books.filter { book -> book.type == Book.types[filter] } }
    }

    fun getCovers() = covers
}