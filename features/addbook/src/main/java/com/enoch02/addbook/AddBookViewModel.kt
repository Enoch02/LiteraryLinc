package com.enoch02.addbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(private val bookDao: BookDao) : ViewModel() {

    fun addNewBook(book: Book) {
        viewModelScope.launch {
            bookDao.insertBook(book)
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            bookDao.insertBook(book)
        }
    }
}