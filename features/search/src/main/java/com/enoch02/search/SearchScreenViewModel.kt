package com.enoch02.search

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.SearchHistoryDao
import com.enoch02.database.model.Book
import com.enoch02.database.model.HistoryItem
import com.enoch02.search_api.Doc
import com.enoch02.search_api.SearchApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "SearchScreenViewModel"

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val searchApiService: SearchApiService,
    private val bookDao: BookDao,
    private val searchHistoryDao: SearchHistoryDao,
    private val bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    val searchQuery = mutableStateOf("")
    var searchState = mutableStateOf(SearchState.NOT_SEARCHING)
    val searchResults = mutableStateListOf<Doc>()
    var active = mutableStateOf(false)

    val history = searchHistoryDao.getHistory()

    fun startSearch(query: String): Result<Unit>? {
        val res = MutableLiveData<Result<Unit>>()

        if (query.isEmpty()) {
            return Result.failure(Exception("Enter a title"))
        }

        searchState.value = SearchState.SEARCHING

        viewModelScope.launch {
            try {
                val result = searchApiService.search(query)

                searchResults.addAll(result.docs)
                searchState.value = SearchState.COMPLETE

                res.postValue(Result.success(Unit))
                Log.d(TAG, "startSearch: Found ${result.numFound}")
            } catch (e: Exception) {
                res.postValue(Result.failure(e))
                searchState.value = SearchState.FAILURE
            }
        }

        return res.value
    }

    fun clearResults() {
        searchState.value = SearchState.NOT_SEARCHING
        searchResults.clear()
    }

    fun isTitleInDb(title: String) = bookDao.checkBookTitle(title)

    fun getTitleIdFromDb(title: String) = bookDao.getIdByTitle(title)

    /**
     * [doc] - a single search result item.
     * [onError] - function to call when an error occurs. Takes a single message and returns a boolean that indicates that the user wants to retry.
     */
    fun addResultToDatabase(
        doc: Doc,
        onError: suspend (message: String, actionLabel: String) -> Boolean
    ) {
        var newBook = Book(
            title = doc.title ?: "Null",
            author = doc.author?.first() ?: "",
            isbn = doc.isbn?.first() ?: ""
        )

        viewModelScope.launch {
            // download cover / just add the book if retry is rejected
            viewModelScope.launch {
                bookCoverRepository.downloadCover("https://covers.openlibrary.org/b/id/${doc.coverId}-M.jpg")
                    .onSuccess {
                        newBook = newBook.copy(coverImageName = it)
                        bookDao.insertBook(newBook)
                    }
                    .onFailure { e ->
                        e.message?.let { message ->
                            if (onError(message, "Retry")) {
                                addResultToDatabase(doc = doc, onError = onError)
                            } else {
                                bookDao.insertBook(newBook)
                            }
                        }
                    }
            }
        }
    }

    fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            history.collect { history ->
                val item = history.firstOrNull() { it.value == query }

                if (item != null) {
                    searchHistoryDao.updateQuery(item.copy(timestamp = System.currentTimeMillis()))
                } else {
                    searchHistoryDao.insertQuery(HistoryItem(value = query))
                }

                cancel()
            }
        }
    }

    enum class SearchState {
        NOT_SEARCHING,
        SEARCHING,
        COMPLETE,
        FAILURE
    }
}