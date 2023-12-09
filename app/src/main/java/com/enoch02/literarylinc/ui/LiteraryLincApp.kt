package com.enoch02.literarylinc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.enoch02.booklist.BookListScreen
import com.enoch02.database.model.Sorting
import com.enoch02.literarylinc.R
import com.enoch02.literarylinc.navigation.Screen
import com.enoch02.literarylinc.navigation.TopLevelDestination
import com.enoch02.more.MoreScreen
import com.enoch02.search.SearchScreen

/**
 * TODO: replace all [androidx.compose.ui.graphics.vector.ImageVector] icons
 * with [painterResource]?
 * TODO: find an efficient or recommended method of preloading the app settings.
 * TODO: Can i animate the changing of themes?
 * TODO: remove the extended material icons dependency (eventually...)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteraryLincApp(navController: NavController) {
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TopLevelDestination.BOOK_LIST) }
    var sorting by rememberSaveable { mutableStateOf(Sorting.ALPHABETICAL) }
    var showSortOptions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentScreen) {
                            TopLevelDestination.BOOK_LIST -> {
                                stringResource(id = R.string.your_library_label)
                            }

                            TopLevelDestination.SEARCH -> {
                                stringResource(id = R.string.search_label)
                            }

                            TopLevelDestination.STATS -> {
                                stringResource(id = R.string.statistics_label)
                            }

                            TopLevelDestination.MORE -> {
                                stringResource(R.string.more_label)
                            }
                        }
                    )
                },
                actions = {
                    if (currentScreen == TopLevelDestination.BOOK_LIST) {
                        IconButton(
                            onClick = { navController.navigate(Screen.BarcodeScanner.route) },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.barcode_scanner_24px),
                                    contentDescription = stringResource(R.string.barcode_scanner_desc)
                                )
                            }
                        )

                        IconButton(
                            onClick = { showSortOptions = true },
                            content = {
                                Icon(
                                    painter = painterResource(id = R.drawable.round_sort_24),
                                    contentDescription = stringResource(R.string.sort_desc)
                                )
                            }
                        )

                        if (showSortOptions) {
                            AlertDialog(
                                title = { Text(text = stringResource(R.string.sorting_options_text)) },
                                onDismissRequest = { showSortOptions = false },
                                confirmButton = {},
                                dismissButton = {
                                    TextButton(
                                        onClick = { showSortOptions = false },
                                        content = {
                                            Text(text = "Cancel")
                                        }
                                    )
                                },
                                text = {
                                    val options = Sorting.values()

                                    Column {
                                        options.forEach {
                                            val onClick = {
                                                showSortOptions = false
                                                sorting = it
                                            }

                                            ListItem(
                                                leadingContent = {
                                                    RadioButton(
                                                        selected = it == sorting,
                                                        onClick = { onClick() }
                                                    )
                                                },
                                                headlineContent = {
                                                    Text(text = it.name.lowercase()
                                                        .replaceFirstChar { c -> c.uppercase() }
                                                        .replace("_", " "))
                                                },
                                                modifier = Modifier.clickable { onClick() }
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            val screens = TopLevelDestination.values()
            val icons = listOf(
                Icons.Rounded.ListAlt,
                Icons.Rounded.Search,
                Icons.Rounded.Analytics,
                Icons.Rounded.MoreHoriz
            )
            val labels = listOf(
                stringResource(R.string.book_list_label),
                stringResource(R.string.search_label),
                stringResource(R.string.statistics_label),
                stringResource(R.string.more_label)
            )

            NavigationBar {
                screens.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = screen == currentScreen,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(imageVector = icons[index], contentDescription = null)
                        },
                        label = {
                            Text(
                                text = labels[index],
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == TopLevelDestination.BOOK_LIST) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddBook.route) },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add_new_book_desc)
                        )
                    },
                )
            }
        },
        content = { paddingValues ->
            Crossfade(
                targetState = currentScreen,
                content = {
                    when (it) {
                        TopLevelDestination.BOOK_LIST -> {
                            BookListScreen(
                                modifier = Modifier.padding(paddingValues),
                                scope = scope,
                                sorting = sorting,
                                onItemClick = { id ->
                                    navController.navigate(Screen.BookDetail.withArgs(id.toString()))
                                }
                            )
                        }

                        TopLevelDestination.SEARCH -> {
                            SearchScreen(modifier = Modifier.padding(paddingValues))
                        }

                        TopLevelDestination.STATS -> {

                        }

                        TopLevelDestination.MORE -> {
                            MoreScreen(
                                navController = navController,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                },
                label = "MainScaffold Cross-fade"
            )
        }
    )
}