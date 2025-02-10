package com.enoch02.stats.stats

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.StatsDao
import com.enoch02.database.model.Book
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class StatsScreenViewModel @Inject constructor(
    private val statsDao: StatsDao,
    private val bookDao: BookDao
) : ViewModel() {
    private val booksFlow = bookDao.getBooks()
    var totalCount by mutableIntStateOf(0)
    var pagesReadCount by mutableIntStateOf(0)
    var currentlyReadingCount by mutableIntStateOf(0)
    var totalHoursRead by mutableIntStateOf(0)
    var fastestCompletedBook by mutableStateOf("")

    init {
        getOtherStats()
    }

    private fun getOtherStats() {
        viewModelScope.launch(Dispatchers.IO) {
            booksFlow.collect { books ->
                totalCount = books.size
                pagesReadCount = books.sumOf { it.pagesRead }
                currentlyReadingCount = books.filter { it.status == Book.status[0] }.size
                computeTotalHoursRead(books)
                computeFastestCompletedBook(books)
            }
        }
    }

    private fun computeTotalHoursRead(books: List<Book>) {
        totalHoursRead = 0
        books.forEach { book ->
            if (book.dateStarted != null && book.dateCompleted != null) {
                val difference = book.dateCompleted!! - book.dateStarted!!
                totalHoursRead += TimeUnit.MILLISECONDS.toHours(difference).toInt()
            }
        }
    }

    private fun computeFastestCompletedBook(books: List<Book>) {
        fastestCompletedBook = ""
        val filteredBooks =
            books.filter { it.dateCompleted != null && it.dateStarted != null && it.status == Book.status[1] }
        val fastest = filteredBooks.minByOrNull { book ->
            book.dateStarted!! - book.dateCompleted!!
        }
        fastestCompletedBook = fastest?.title ?: ""
    }
}