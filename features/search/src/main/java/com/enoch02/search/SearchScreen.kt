package com.enoch02.search

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.components.SearchResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize(),
        content = {
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                active = false,
                onActiveChange = { },
                onSearch = {
                    viewModel.startSearch(searchQuery)
                        ?.onFailure {
                            Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
                        }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    viewModel.clearResults()
                                }
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                },
                shape = RectangleShape,
                placeholder = { Text(text = "Book title") },
                modifier = Modifier.fillMaxWidth(),
                content = {

                }
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
                content = {
                    //TODO: might remove crossfade
                    Crossfade(
                        targetState = viewModel.searchState.value,
                        content = {
                            when (it) {
                                SearchScreenViewModel.SearchState.SEARCHING -> {
                                    CircularProgressIndicator()
                                }

                                SearchScreenViewModel.SearchState.NOT_SEARCHING -> {
                                    Text(
                                        text = "Enter a book title and tap the search button on your keyboard to find books",
                                        textAlign = TextAlign.Center
                                    )
                                }

                                SearchScreenViewModel.SearchState.COMPLETE -> {
                                    LazyColumn(
                                        content = {
                                            items(
                                                count = viewModel.searchResults.size,
                                                itemContent = { index ->
                                                    val item =
                                                        viewModel.searchResults[index]

                                                    SearchResultItem(
                                                        title = item.title ?: "",
                                                        author = item.author ?: emptyList(),
                                                        /*TODO: Add settings option to set image quality [S, M, L]*/
                                                        coverUrl = "https://covers.openlibrary.org/b/id/${item.coverId}-M.jpg"
                                                    )
                                                }
                                            )
                                        }
                                    )
                                }

                                SearchScreenViewModel.SearchState.FAILURE -> {
                                    Text(
                                        text = "Unable to fetch results",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        label = "Search results crossfade"
                    )
                }
            )
        }
    )
}