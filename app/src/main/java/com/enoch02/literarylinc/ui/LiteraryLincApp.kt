package com.enoch02.literarylinc.ui


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.ListAlt
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.enoch02.booklist.BookListScreen
import com.enoch02.booklist.components.BookListViewMode
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.R
import com.enoch02.literarylinc.navigation.Screen
import com.enoch02.literarylinc.navigation.TopLevelDestination
import com.enoch02.more.MoreScreen
import com.enoch02.reader.ReaderScreen
import com.enoch02.search.SearchScreen
import com.enoch02.stats.StatsScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TODO: replace all [androidx.compose.ui.graphics.vector.ImageVector] icons
 * with [painterResource]?
 * TODO: find an efficient or recommended method of preloading the app settings.
 * TODO: Can i animate the changing of themes?
 * TODO: remove the extended material icons dependency (eventually...)
 * TODO: Consider sharing the placeholder image in booklist and bookdetail modules
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteraryLincApp(navController: NavController) {
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TopLevelDestination.BOOK_LIST) }
    var sorting by rememberSaveable { mutableStateOf(Sorting.ALPHABETICAL) }
    var showSortOptions by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var bookListViewMode by rememberSaveable { mutableStateOf(BookListViewMode.LIST_VIEW) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var statusFilter by rememberSaveable { mutableStateOf(StatusFilter.ALL) }
    var enableDrawerGestures by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(
        key1 = currentScreen,
        block = {
            enableDrawerGestures = currentScreen == TopLevelDestination.BOOK_LIST
        }
    )

    ModalNavigationDrawer(
        gesturesEnabled = enableDrawerGestures,
        drawerContent = {
            val statusFilters = StatusFilter.values()

            ModalDrawerSheet(
                drawerShape = RectangleShape,
                content = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        modifier = Modifier.padding(16.dp),
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    statusFilters.forEach {
                        NavigationDrawerItem(
                            label = {
                                Text(text = it.stringify())
                            },
                            selected = statusFilter == it,
                            onClick = {
                                statusFilter = it
                                scope.launch { drawerState.close() }
                            },
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                bottomStart = 4.dp,
                                topEnd = 24.dp,
                                bottomEnd = 24.dp
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                })
        },
        drawerState = drawerState,
        content = {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentScreen) {
                                    TopLevelDestination.BOOK_LIST -> {
                                        when (statusFilter) {
                                            StatusFilter.ALL -> {
                                                stringResource(id = R.string.your_library_label)
                                            }

                                            StatusFilter.READING -> {
                                                StatusFilter.READING.stringify()
                                            }

                                            StatusFilter.COMPLETED -> {
                                                StatusFilter.COMPLETED.stringify()
                                            }

                                            StatusFilter.ON_HOLD -> {
                                                StatusFilter.ON_HOLD.stringify()
                                            }

                                            StatusFilter.PLANNING -> {
                                                StatusFilter.PLANNING.stringify()
                                            }
                                        }
                                    }

                                    TopLevelDestination.READER -> {
                                        stringResource(R.string.reader_label)
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
                            when (currentScreen) {
                                TopLevelDestination.BOOK_LIST -> {
                                    IconButton(
                                        onClick = {
                                            bookListViewMode = when (bookListViewMode) {
                                                BookListViewMode.LIST_VIEW -> {
                                                    BookListViewMode.GRID_VIEW
                                                }

                                                BookListViewMode.GRID_VIEW -> {
                                                    BookListViewMode.LIST_VIEW
                                                }
                                            }
                                        },
                                        content = {
                                            Icon(
                                                painter = when (bookListViewMode) {
                                                    BookListViewMode.LIST_VIEW -> {
                                                        painterResource(id = R.drawable.round_grid_view_24)
                                                    }

                                                    BookListViewMode.GRID_VIEW -> {
                                                        painterResource(id = R.drawable.round_view_list_24)
                                                    }
                                                },
                                                contentDescription = stringResource(R.string.toggle_arrangement_desc)
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
                                                                Text(text = it.stringify())
                                                            },
                                                            modifier = Modifier.clickable { onClick() }
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }

                                TopLevelDestination.READER -> {

                                }

                                TopLevelDestination.SEARCH -> {
                                    IconButton(
                                        onClick = { navController.navigate(Screen.BarcodeScanner.route) },
                                        content = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.barcode_scanner_24px),
                                                contentDescription = stringResource(R.string.barcode_scanner_desc)
                                            )
                                        }
                                    )
                                }

                                TopLevelDestination.STATS -> {
                                    /*TODO()*/
                                }

                                TopLevelDestination.MORE -> {
                                    /*TODO()*/
                                }
                            }
                        },
                        navigationIcon = {
                            if (currentScreen == TopLevelDestination.BOOK_LIST) {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            if (drawerState.isClosed) {
                                                drawerState.open()
                                            } else {
                                                drawerState.close()
                                            }
                                        }
                                    },
                                    content = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.round_menu_24),
                                            contentDescription = stringResource(R.string.status_filter_menu_desc)
                                        )
                                    }
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    val screens = TopLevelDestination.values()
                    val icons = listOf(
                        Icons.Rounded.ListAlt,
                        Icons.Rounded.InsertDriveFile,
                        Icons.Rounded.Search,
                        Icons.Rounded.Analytics,
                        Icons.Rounded.MoreHoriz
                    )
                    val labels = listOf(
                        stringResource(R.string.book_list_label),
                        stringResource(R.string.reader_label),
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
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
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
                                        statusFilter = statusFilter,
                                        listViewMode = bookListViewMode,
                                        listState = rememberLazyListState(),
                                        gridState = rememberLazyGridState(),
                                        onItemClick = { id ->
                                            navController.navigate(Screen.BookDetail.withArgs(id.toString()))
                                        }
                                    )
                                }

                                TopLevelDestination.READER -> {
                                    ReaderScreen(
                                        modifier = Modifier.padding(paddingValues),
                                        navController = navController
                                    )
                                }

                                TopLevelDestination.SEARCH -> {
                                    SearchScreen(
                                        modifier = Modifier.padding(paddingValues),
                                        onEdit = { id ->
                                            navController.navigate(Screen.BookDetail.withArgs(id.toString()))
                                        },
                                        onError = { message, actionLabel ->
                                            return@SearchScreen withContext(scope.coroutineContext) {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = message,
                                                    actionLabel = actionLabel,
                                                    duration = SnackbarDuration.Short
                                                )

                                                when (result) {
                                                    SnackbarResult.ActionPerformed -> {
                                                        return@withContext true
                                                    }

                                                    SnackbarResult.Dismissed -> {
                                                        return@withContext false
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }

                                TopLevelDestination.STATS -> {
                                    StatsScreen(
                                        navController = navController,
                                        modifier = Modifier.padding(paddingValues)
                                    )
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
    )
}