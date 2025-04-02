package com.enoch02.literarylinc.ui


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.booklist.BookListScreen
import com.enoch02.booklist.components.BookViewMode
import com.enoch02.database.model.ReaderSorting
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import com.enoch02.literarylinc.RequestNotificationPermission
import com.enoch02.literarylinc.navigation.Screen
import com.enoch02.literarylinc.navigation.TopLevelDestination
import com.enoch02.literarylinc.ui.components.BookListSortOptionsAlert
import com.enoch02.literarylinc.ui.components.BookListTopAppBar
import com.enoch02.literarylinc.ui.components.MoreTopAppBar
import com.enoch02.literarylinc.ui.components.ReaderListSortOptionsAlert
import com.enoch02.literarylinc.ui.components.ReaderTopAppBar
import com.enoch02.literarylinc.ui.components.drawersheets.BookListDrawerSheet
import com.enoch02.literarylinc.ui.components.drawersheets.ReaderListDrawerSheet
import com.enoch02.more.MoreScreen
import com.enoch02.more.navigation.MoreScreenDestination
import com.enoch02.reader.ReaderListScreen
import com.enoch02.resources.LLString
import com.enoch02.stats.components.StatsTopAppBar
import com.enoch02.stats.stats.StatsScreen
import kotlinx.coroutines.launch

@Composable
fun LiteraryLincApp(navController: NavController, viewModel: LLAppViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf(TopLevelDestination.READER) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    //book list
    var currentBookListSorting by rememberSaveable { mutableStateOf(Sorting.ALPHABETICAL) }
    var showBookListSortOptions by rememberSaveable { mutableStateOf(false) }
    var bookViewMode by rememberSaveable { mutableStateOf(BookViewMode.LIST_VIEW) }
    var statusFilter by rememberSaveable { mutableStateOf(StatusFilter.ALL) }
    var isSearchingInBookList by rememberSaveable { mutableStateOf(false) }

    // reader list
    var currentReaderListSorting by rememberSaveable { mutableStateOf(ReaderSorting.LAST_READ) }
    var showReaderListSortOptions by rememberSaveable { mutableStateOf(false) }
    val currentReaderListFilter by viewModel.getCurrentReaderFilter()
        .collectAsState(initial = null)
    var isSearchingInReaderList by rememberSaveable { mutableStateOf(false) }

    var enableDrawerGestures by rememberSaveable { mutableStateOf(true) }

    // stats
    val readingGoal by viewModel.readingGoal.collectAsState(0)
    val readingProgress by viewModel.readingProgress.collectAsState(0)

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
                            viewModel.changeReaderFilter(selected)
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
                                bookViewMode = bookViewMode,
                                onChangeBookListMode = { mode ->
                                    bookViewMode = mode
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
                                },
                                onSearch = {
                                    isSearchingInBookList = true
                                }
                            )
                        }

                        TopLevelDestination.READER -> {
                            currentReaderListFilter?.let {
                                ReaderTopAppBar(
                                    readerFilter = it,
                                    onSearch = { isSearchingInReaderList = true },
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
                        }

                        TopLevelDestination.STATS -> {
                            StatsTopAppBar(
                                readingGoal = readingGoal,
                                readingProgress = readingProgress,
                                onSaveProgressData = { goal, progress ->
                                    viewModel.updateReadingGoalData(goal, progress)
                                }
                            )
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
                        Icons.Rounded.Analytics,
                        Icons.Rounded.MoreHoriz
                    )
                    val labels = listOf(
                        stringResource(LLString.bookList),
                        stringResource(LLString.reader),
                        stringResource(LLString.statistics),
                        stringResource(LLString.more)
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
                                    contentDescription = stringResource(LLString.addBookDesc)
                                )
                            },
                        )
                    }
                },
                content = { paddingValues ->
                    RequestNotificationPermission()

                    Crossfade(
                        targetState = currentScreen,
                        content = {
                            when (it) {
                                TopLevelDestination.BOOK_LIST -> {
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
                                        listViewMode = bookViewMode,
                                        onItemClick = { id ->
                                            navController.navigate(Screen.BookDetail.withArgs(id.toString()))
                                        },
                                        onItemEdit = { id ->
                                            navController.navigate(Screen.EditBook.withArgs(id.toString()))
                                        },
                                        isSearching = isSearchingInBookList,
                                        onDismissSearching = {
                                            isSearchingInBookList = false
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

                                    when (currentReaderListFilter) {
                                        null -> {
                                            CircularProgressIndicator()
                                        }

                                        else -> {
                                            ReaderListScreen(
                                                modifier = Modifier.padding(paddingValues),
                                                sorting = currentReaderListSorting,
                                                filter = currentReaderListFilter!!,
                                                isSearching = isSearchingInReaderList,
                                                onScanForDocs = {
                                                    navController.navigate(MoreScreenDestination.FileScan.route)
                                                },
                                                onDismissSearching = {
                                                    isSearchingInReaderList = false
                                                }
                                            )
                                        }
                                    }
                                }

                                TopLevelDestination.STATS -> {
                                    StatsScreen(modifier = Modifier.padding(paddingValues))
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