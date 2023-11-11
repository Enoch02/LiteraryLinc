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

    suspend fun addNewBook(
        title: String,
        author: String,
        category: String,
        pagesRead: String,
        pageCount: String, /* dateStarted:*/
        timesReread: String,
        personalRating: String,
        isbn: String,
        genre: String,
        type: String,
        coverImageUri: Uri?,
        description: String,
        status: String
    ): Result<Unit> {
        try {
            val fileName = coverImageUri?.let { bookCoverRepository.copyCover(it) }
            val newBook = Book.createBook(
                title = title,
                author = author,
                category = category,
                pagesRead = pagesRead,
                pageCount = pageCount,
                timesReread = timesReread,
                personalRating = personalRating,
                isbn = isbn,
                genre = genre,
                type = type,
                coverImageName = fileName,
                description = description,
                status = status
            )
            bookDao.insertBook(newBook)

        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }
}