package com.enoch02.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) : ViewModel() {
    private val covers = bookCoverRepository.latestCoverPath

    suspend fun getBook(id: Int): Book {
        return withContext(viewModelScope.coroutineContext) {
            bookDao.getBookById(id)
        }
    }

    suspend fun getCover(coverName: String): String? = covers.first()[coverName]

    fun deleteBook(id: Int) = viewModelScope.launch { bookDao.deleteBook(id) }
}