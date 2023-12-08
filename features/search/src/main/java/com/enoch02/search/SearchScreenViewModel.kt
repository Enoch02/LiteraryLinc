package com.enoch02.search

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.search_api.SearchApiService
import com.enoch02.search_api.Doc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchScreenViewModel @Inject constructor(private val searchApiService: SearchApiService): ViewModel() {
    val searchResults = mutableStateListOf<Doc>()

    fun startSearch(query: String) {
        viewModelScope.launch {
            val result = searchApiService.search(query)
            searchResults.addAll(result.docs)
        }
    }
}