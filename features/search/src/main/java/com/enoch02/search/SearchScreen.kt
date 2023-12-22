package com.enoch02.search

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.components.DocDetail
import com.enoch02.components.SearchResultItem
import kotlinx.coroutines.launch

/***
 * [onError] - Callback used to show a snackbar with desired message when an error occurs.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    modifier: Modifier,
    onEdit: (id: Int) -> Unit,
    onError: suspend (message: String, actionLabel: String) -> Boolean,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by viewModel.searchQuery
    val keyboardController = LocalSoftwareKeyboardController.current
    val history by viewModel.history.collectAsState(initial = emptyList())
    var active by viewModel.active

    val onSearch = {
        searchQuery = searchQuery.trim()
        active = false
        keyboardController?.hide()
        viewModel.startSearch(searchQuery)
            ?.onFailure {
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
            }
        viewModel.addToSearchHistory(query = searchQuery)
    }


    Column(
        modifier = modifier.fillMaxSize(),
        content = {
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    active = true
                },
                active = active,
                onActiveChange = { active = !active },
                onSearch = { onSearch() },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                searchQuery = ""
                                viewModel.clearResults()
                            } else {
                                active = false
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = null
                            )
                        }
                    )
                },
                shape = RectangleShape,
                placeholder = { Text(text = "Book title") },
                modifier = Modifier.fillMaxWidth(),
                content = {

                    LazyColumn(
                        content = {
                            items(
                                count = history.size,
                                itemContent = { index ->
                                    ListItem(
                                        leadingContent = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.round_history_24),
                                                contentDescription = null
                                            )
                                        },
                                        headlineContent = { Text(text = history[index].value) },
                                        modifier = Modifier.clickable {
                                            searchQuery = history[index].value
                                            onSearch()
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )

            Crossfade(
                targetState = viewModel.searchState.value,
                content = {
                    when (it) {
                        SearchScreenViewModel.SearchState.SEARCHING -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                                content = { CircularProgressIndicator() }
                            )
                        }

                        SearchScreenViewModel.SearchState.NOT_SEARCHING -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    Text(
                                        text = "Enter a book title and tap the search button on your keyboard to find books",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }

                        SearchScreenViewModel.SearchState.COMPLETE -> {
                            if (viewModel.searchResults.isEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize(),
                                    content = {
                                        Text(
                                            text = "No result found",
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                )
                            } else {
                                LazyColumn(
                                    content = {
                                        items(
                                            count = viewModel.searchResults.size,
                                            itemContent = { index ->
                                                val item = viewModel.searchResults[index]
                                                val isItemInDb =
                                                    viewModel.isTitleInDb(item.title.toString())
                                                        .collectAsState(initial = false)
                                                val id =
                                                    viewModel.getTitleIdFromDb(item.title.toString())
                                                        .collectAsState(
                                                            initial = 0
                                                        )
                                                /*TODO: Add settings option to set image quality [S, M, L]*/
                                                val coverUrl =
                                                    "https://covers.openlibrary.org/b/id/${item.coverId}-M.jpg"
                                                var showDetailDialog by rememberSaveable {
                                                    mutableStateOf(
                                                        false
                                                    )
                                                }

                                                SearchResultItem(
                                                    title = item.title ?: "",
                                                    author = item.author ?: emptyList(),
                                                    coverUrl = coverUrl,
                                                    itemInDatabase = isItemInDb.value,
                                                    onClick = {
                                                        showDetailDialog = true
                                                    },
                                                    onAddBtnClick = {
                                                        viewModel.addResultToDatabase(
                                                            doc = item,
                                                            onError = onError
                                                        )

                                                        scope.launch {
                                                            // No error here 🙂
                                                            onError(
                                                                "Adding to database, please wait",
                                                                ""
                                                            )
                                                        }
                                                    },
                                                    onEditBtnClick = {
                                                        onEdit(id.value)
                                                    }
                                                )

                                                if (showDetailDialog) {
                                                    DocDetail(
                                                        doc = item,
                                                        coverUrl = coverUrl,
                                                        onDismiss = { showDetailDialog = false },
                                                        onConfirm = {
                                                            /*TODO*/
                                                        }
                                                    )
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }

                        SearchScreenViewModel.SearchState.FAILURE -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    Text(
                                        text = "Unable to fetch results",
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }
                    }
                },
                label = "Search results crossfade"
            )
        }
    )
}