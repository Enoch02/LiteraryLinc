package com.enoch02.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.BookType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(private val bookDao: BookDao) : ViewModel() {
    val books = bookDao.getBooks()

}