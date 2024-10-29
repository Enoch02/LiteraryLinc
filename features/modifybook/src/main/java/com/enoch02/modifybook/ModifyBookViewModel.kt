package com.enoch02.modifybook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ModifyBookViewModel @Inject constructor(
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

    suspend fun addBook(
        title: String,
        author: String,
        pagesRead: String,
        pageCount: String,
        dateStarted: Long?,
        dateCompleted: Long?,
        timesReread: String,
        personalRating: String,
        isbn: String,
        genre: String,
        type: String,
        coverImageUri: Uri?,
        notes: String,
        status: String,
        volumesRead: String,
        totalVolumes: String
    ): Result<Unit> {
        try {
            val fileName = coverImageUri?.let { bookCoverRepository.copyCover(it) }
            val newBook = Book.createBook(
                title = title,
                author = author,
                pagesRead = pagesRead,
                pageCount = pageCount,
                dateStarted = dateStarted,
                dateCompleted = dateCompleted,
                timesReread = timesReread,
                personalRating = personalRating,
                isbn = isbn,
                genre = genre,
                type = type,
                coverImageName = fileName,
                notes = notes,
                status = status,
                volumesRead = volumesRead,
                totalVolumes = totalVolumes
            )

            bookDao.insertBook(newBook)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }

    suspend fun updateBook(
        id: Int,
        title: String,
        author: String,
        pagesRead: String,
        pageCount: String,
        dateStarted: Long?,
        dateCompleted: Long?,
        timesReread: String,
        personalRating: String,
        isbn: String,
        genre: String,
        type: String,
        coverImageUri: Uri?,
        coverImageName: String?,
        notes: String,
        status: String,
        volumesRead: String,
        totalVolumes: String,
        documentMd5: String?
    ): Result<Unit> {
        try {
            val fileName = coverImageUri?.let { bookCoverRepository.copyCover(it) }
            val book = Book.createBook(
                id = id,
                title = title,
                author = author,
                pagesRead = pagesRead,
                pageCount = pageCount,
                dateStarted = dateStarted,
                dateCompleted = dateCompleted,
                timesReread = timesReread,
                personalRating = personalRating,
                isbn = isbn,
                genre = genre,
                type = type,
                coverImageName = if (coverImageUri == null) coverImageName else fileName,
                notes = notes,
                status = status,
                volumesRead = volumesRead,
                totalVolumes = totalVolumes,
                documentMd5 = documentMd5
            )

            bookDao.updateBook(book)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(Unit)
    }
}