package com.enoch02.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(modifier: Modifier, viewModel: SearchScreenViewModel = hiltViewModel()) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        content = {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                active = active,
                onActiveChange = { active = it },
                onSearch = { viewModel.startSearch(searchQuery) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (active) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
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
                    }
                },
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth(),
                content = {
                    LazyColumn(
                        content = {
                            items(
                                count = viewModel.searchResults.size,
                                itemContent = { index ->
                                    ListItem(
                                        headlineContent = { Text(text = viewModel.searchResults[index].title) },
                                        supportingContent = { Text(text = viewModel.searchResults[index].author.first()) },
                                        modifier = Modifier.clickable {

                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )

        }
    )
}