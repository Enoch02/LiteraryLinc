package com.enoch02.search

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.coverfile.BookCoverRepository
import com.enoch02.database.dao.BookDao
import com.enoch02.search_api.SearchApiService
import com.enoch02.search_api.Doc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val TAG = "SearchScreenViewModel"

@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val searchApiService: SearchApiService,
    private val bookDao: BookDao,
    private val bookCoverRepository: BookCoverRepository
) :
    ViewModel() {
    val searchQuery = mutableStateOf("")
    var searchState = mutableStateOf(SearchState.NOT_SEARCHING)
    val searchResults = mutableStateListOf<Doc>()

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

    fun clearResults() = searchResults.clear()

    fun addResultToDatabase(doc: Doc) {
        bookCoverRepository.downloadCover("https://covers.openlibrary.org/b/id/${doc.coverId}-M.jpg")
    }

    enum class SearchState {
        NOT_SEARCHING,
        SEARCHING,
        COMPLETE,
        FAILURE
    }
}