package com.artifex.mupdf.viewer.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.artifex.mupdf.viewer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    documentTitle: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onNextSearchResult: () -> Unit,
    onPreviousSearchResult: () -> Unit,
    searchInProgress: Boolean,
    onLink: () -> Unit,
    hasOutline: Boolean,
    onOutline: () -> Unit
) {
    var isSearching by remember { mutableStateOf(false) }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        content = {
            AnimatedContent(
                isSearching,
                label = "top bar",
                content = {
                    when (it) {
                        true -> {
                            Column(content = {
                                if (searchInProgress) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }

                                SearchTextField(
                                    query = searchQuery,
                                    onQueryChange = { newQuery ->
                                        onSearchQueryChange(newQuery)
                                    },
                                    onNextItem = {
                                        onNextSearchResult()
                                    },
                                    onPrevItem = {
                                        onPreviousSearchResult()
                                    },
                                    onSearch = {
                                        onSearch()
                                    },
                                    onCloseSearch = {
                                        isSearching = false
                                    }
                                )
                            })
                        }

                        false -> {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = documentTitle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                actions = {
                                    IconButton(
                                        content = {
                                            Icon(
                                                imageVector = Icons.Rounded.Link,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = onLink
                                    )

                                    IconButton(
                                        content = {
                                            Icon(
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            isSearching = true
                                        }
                                    )

                                    if (hasOutline) {
                                        IconButton(
                                            content = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.List,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = onOutline
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    )
}

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    onSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onNextItem: () -> Unit,
    onPrevItem: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        value = query,
        onValueChange = { onQueryChange(it) },
        maxLines = 1,
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                keyboardController?.hide()
            }
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        leadingIcon = {
            IconButton(
                onClick = onCloseSearch,
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            )
        },
        trailingIcon = {
            Row {
                IconButton(
                    onClick = onPrevItem,
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                )

                IconButton(
                    onClick = onNextItem,
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                )
            }
        }
    )
}

@Preview
@Composable
private fun Preview() {
    ReaderTopBar(
        visible = true,
        documentTitle = "Hello, World",
        onLink = {},
        onSearch = {},
        onNextSearchResult = {},
        onPreviousSearchResult = {},
        hasOutline = true,
        onOutline = {},
        searchQuery = "",
        onSearchQueryChange = {},
        searchInProgress = true
    )
}