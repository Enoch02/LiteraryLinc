package com.enoch02.literarylinc.ui


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.enoch02.booklist.BookListScreen
import com.enoch02.booklist.components.BookListViewMode
import com.enoch02.database.model.ReaderFilter
import com.enoch02.database.model.ReaderSorting
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.R
import com.enoch02.literarylinc.RequestNotificationPermission
import com.enoch02.literarylinc.navigation.Screen
import com.enoch02.literarylinc.navigation.TopLevelDestination
import com.enoch02.literarylinc.ui.components.BookListSortOptionsAlert
import com.enoch02.literarylinc.ui.components.BookListTopAppBar
import com.enoch02.literarylinc.ui.components.MoreTopAppBar
import com.enoch02.literarylinc.ui.components.ReaderListSortOptionsAlert
import com.enoch02.literarylinc.ui.components.ReaderTopAppBar
import com.enoch02.literarylinc.ui.components.StatsTopAppBar
import com.enoch02.literarylinc.ui.components.drawersheets.BookListDrawerSheet
import com.enoch02.literarylinc.ui.components.drawersheets.ReaderListDrawerSheet
import com.enoch02.more.MoreScreen
import com.enoch02.more.navigation.MoreScreenDestination
import com.enoch02.reader.ReaderScreen
import com.enoch02.search.SearchScreen
import com.enoch02.stats.StatsScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteraryLincApp(navController: NavController) {
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TopLevelDestination.BOOK_LIST) }
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    //book list
    var currentBookListSorting by rememberSaveable { mutableStateOf(Sorting.ALPHABETICAL) }
    var showBookListSortOptions by rememberSaveable { mutableStateOf(false) }
    var bookListViewMode by rememberSaveable { mutableStateOf(BookListViewMode.LIST_VIEW) }
    var statusFilter by rememberSaveable { mutableStateOf(StatusFilter.ALL) }

    // reader list
    var currentReaderListSorting by rememberSaveable { mutableStateOf(ReaderSorting.LAST_READ) }
    var showReaderListSortOptions by rememberSaveable { mutableStateOf(false) }
    // TODO: persist last used value using datastore
    var currentReaderListFilter by rememberSaveable { mutableStateOf(ReaderFilter.ALL) }

    var enableDrawerGestures by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(
        key1 = currentScreen,
        block = {
            enableDrawerGestures =
                currentScreen == TopLevelDestination.BOOK_LIST || currentScreen == TopLevelDestination.READER
        }
    )

    ModalNavigationDrawer(
        gesturesEnabled = enableDrawerGestures,
        drawerContent = {
            when (currentScreen) {
                TopLevelDestination.BOOK_LIST -> {
                    BookListDrawerSheet(
                        selectedStatusFilter = statusFilter,
                        onStatusSelected = { selected ->
                            statusFilter = selected
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                TopLevelDestination.READER -> {
                    ReaderListDrawerSheet(
                        selectedFilter = currentReaderListFilter,
                        onFilterSelected = { selected ->
                            currentReaderListFilter = selected
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                else -> {
                    ModalDrawerSheet {

                    }
                }
            }
        },
        drawerState = drawerState,
        content = {
            Scaffold(
                topBar = {
                    when (currentScreen) {
                        TopLevelDestination.BOOK_LIST -> {
                            BookListTopAppBar(
                                statusFilter = statusFilter,
                                bookListViewMode = bookListViewMode,
                                onChangeBookListMode = { mode ->
                                    bookListViewMode = mode
                                },
                                onShowSortOption = {
                                    showBookListSortOptions = true
                                },
                                onChangeDrawerState = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }
                            )
                        }

                        TopLevelDestination.READER -> {
                            ReaderTopAppBar(
                                readerFilter = currentReaderListFilter,
                                onShowSorting = { showReaderListSortOptions = true },
                                onChangeDrawerState = {
                                    scope.launch {
                                        if (drawerState.isClosed) {
                                            drawerState.open()
                                        } else {
                                            drawerState.close()
                                        }
                                    }
                                }
                            )
                        }

                        TopLevelDestination.SEARCH -> {
                            TopAppBar(title = { Text("Leaving soon...") })
                        }

                        TopLevelDestination.STATS -> {
                            StatsTopAppBar()
                        }

                        TopLevelDestination.MORE -> {
                            MoreTopAppBar()
                        }
                    }
                },
                bottomBar = {
                    val screens = TopLevelDestination.entries.toTypedArray()
                    val icons = listOf(
                        Icons.AutoMirrored.Rounded.ListAlt,
                        Icons.AutoMirrored.Rounded.InsertDriveFile,
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
                                    RequestNotificationPermission()

                                    BookListSortOptionsAlert(
                                        showBookListSortOptions = showBookListSortOptions,
                                        currentBookListSorting = currentBookListSorting,
                                        onDismiss = {
                                            showBookListSortOptions = false
                                        },
                                        onSortingClicked = { picked ->
                                            showBookListSortOptions = false
                                            currentBookListSorting = picked
                                        }
                                    )

                                    BookListScreen(
                                        modifier = Modifier.padding(paddingValues),
                                        scope = scope,
                                        sorting = currentBookListSorting,
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
                                    ReaderListSortOptionsAlert(
                                        showReaderListSortOptions = showReaderListSortOptions,
                                        currentReaderListSorting = currentReaderListSorting,
                                        onDismiss = {
                                            showReaderListSortOptions = false
                                        },
                                        onSortingClicked = { picked ->
                                            showReaderListSortOptions = false
                                            currentReaderListSorting = picked
                                        }
                                    )
                                    ReaderScreen(
                                        modifier = Modifier.padding(paddingValues),
                                        sorting = currentReaderListSorting,
                                        filter = currentReaderListFilter,
                                        onScanForDocs = {
                                            navController.navigate(MoreScreenDestination.Scanner.route)
                                        }
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