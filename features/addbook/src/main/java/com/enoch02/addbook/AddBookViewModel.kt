package com.enoch02.addbook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBookViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) : ViewModel() {
    suspend fun addNewBook(title: String, type: String, coverImageUri: Uri?): Result<Unit> {
        try {
            val fileName = coverImageUri?.let { bookCoverRepository.copyCover(it) }
            val newBook = Book.createBook(
                title = title,
                type = type,
                coverImageName = fileName
            )
            bookDao.insertBook(newBook)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            bookDao.insertBook(book)
        }
    }
}