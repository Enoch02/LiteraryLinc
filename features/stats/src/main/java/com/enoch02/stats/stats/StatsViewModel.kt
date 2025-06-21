package com.enoch02.stats.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.model.Book
import com.enoch02.settings.ReadingProgressManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StatsViewModel @Inject constructor(
    bookDao: BookDao,
    private val readingProgressManager: ReadingProgressManager
) : ViewModel() {
    private val booksFlow = bookDao.getBooks()
    var totalCount by mutableIntStateOf(0)
    var pagesReadCount by mutableIntStateOf(0)
    var currentlyReadingCount by mutableIntStateOf(0)
    var totalHoursRead by mutableIntStateOf(0)
    var fastestCompletedBook by mutableStateOf("")
    var booksReadThisYear by mutableIntStateOf(0)
    var averageRating by mutableIntStateOf(0)

    var currentReadingStreak = readingProgressManager.getReadingStreak()
    var longestReadingStreak = readingProgressManager.getLongestReadingStreak()
    val readingGoal = readingProgressManager.getReadingGoal()
    val readingProgress = readingProgressManager.getReadingGoalProgress()

    init {
        collectOtherStats()
        getStreak()
    }

    fun formatCurrentStreakMessage(value: Int): String {
        if (value == 0) {
            return "Start Reading to build a streak!"
        }

        return buildString {
            if (value > 7) {
                append("🔥")
            }

            append("Reading Streak: $value")
            if (value == 1) {
                append(" day")
            } else {
                append(" days")
            }
        }
    }

    fun formatLongestStreakMessage(value: Int): String {
        if (value == 0) {
            return "None"
        }

        return buildString {
            append(value)

            if (value == 1) {
                append(" day")

            } else {
                append(" days")
            }
        }
    }

    private fun collectOtherStats() {
        viewModelScope.launch(Dispatchers.IO) {
            booksFlow.collect { books ->
                totalCount = books.size
                pagesReadCount = books.sumOf { it.pagesRead }
                currentlyReadingCount =
                    books.filter { it.status == Book.Companion.BookStatus.READING.strName }.size
                computeTotalHoursRead(books)
                computeFastestCompletedBook(books)
                booksReadThisYear = computeBooksReadThisYear(books)
                averageRating = computeAverageRating(books)
            }
        }
    }

    private fun getStreak(): Flow<Int> {
        return readingProgressManager.getReadingStreak()
    }

    private fun computeTotalHoursRead(books: List<Book>) {
        books.forEach { book ->
            if (book.dateStarted != null && book.dateCompleted != null) {
                val difference = book.dateCompleted!! - book.dateStarted!!
                totalHoursRead += TimeUnit.MILLISECONDS.toHours(difference).toInt()
            }
        }
    }

    private fun computeFastestCompletedBook(books: List<Book>) {
        val filteredBooks =
            books.filter { it.dateCompleted != null && it.dateStarted != null && it.status == Book.Companion.BookStatus.COMPLETED.strName }
        val fastest = filteredBooks.minByOrNull { book ->
            book.dateCompleted!! - book.dateStarted!!
        }

        fastestCompletedBook = fastest?.title ?: ""
    }

    private fun computeBooksReadThisYear(books: List<Book>): Int {
        return books.filter { isThisYear(it.dateStarted) && it.status == Book.Companion.BookStatus.COMPLETED.strName }.size
    }

    private fun computeAverageRating(books: List<Book>): Int {
        val avg = books.map { it.personalRating }.filter { it > 0 }.average()

        return if (avg.isNaN()) {
            0
        } else {
            avg.roundToInt()
        }
    }

    private fun isThisYear(timestamp: Long?): Boolean {
        if (timestamp == null) {
            return false
        }

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = timestamp
        val yearFromTimestamp = calendar.get(Calendar.YEAR)

        return yearFromTimestamp == currentYear
    }

    fun updateReadingStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            readingProgressManager.updateReadingStreak(isBookOpen = false)
        }
    }
}

fun Int.withCommas(): String = NumberFormat.getNumberInstance(Locale.ROOT).format(this)