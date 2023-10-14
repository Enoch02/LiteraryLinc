package com.enoch02.booklist

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.BookType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(private val bookDao: BookDao) : ViewModel() {
    private val books = bookDao.getBooks()

    fun getBooks(filter: Int): Flow<List<Book>> {
        if (filter == 0) {
            return books
        }

        return books.map { books -> books.filter { book -> book.type.ordinal == filter } }
    }
}