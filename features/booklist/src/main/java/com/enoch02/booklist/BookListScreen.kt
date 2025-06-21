package com.enoch02.booklist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.booklist.components.BookGridView
import com.enoch02.booklist.components.BookListView
import com.enoch02.booklist.components.BookViewMode
import com.enoch02.booklist.components.BooklistBottomSheet
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
import com.enoch02.database.model.StatusFilter
import com.enoch02.resources.composables.ListSelectionTopRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun BookListScreen(
    modifier: Modifier,
    scope: CoroutineScope,
    sorting: Sorting,
    statusFilter: StatusFilter,
    listViewMode: BookViewMode,
    onItemClick: (Int) -> Unit,
    onItemEdit: (Int) -> Unit,
    isSearching: Boolean,
    onDismissSearching: () -> Unit,
    viewModel: BookListViewModel = hiltViewModel()
) {
    val tabLabels = Book.Companion.BookType.entries.map { it.strName }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabLabels.size })
    val covers = viewModel.getCovers()
        .collectAsState(initial = emptyMap()).value

    // clear selected items when filter changes
    LaunchedEffect(pagerState.currentPage) {
        viewModel.clearSelectedBooks()
    }

    Column(
        modifier = modifier.fillMaxSize(),
        content = {
            ListSelectionTopRow(
                visible = viewModel.selectedBooks.isNotEmpty(),
                selectionCount = viewModel.selectedBooks.size,
                onClearSelection = { viewModel.clearSelectedBooks() },
                onSelectAll = { viewModel.selectAllBooks(currentType = pagerState.currentPage) },
                onInvertSelection = { viewModel.invertSelection(currentType = pagerState.currentPage) },
                onDelete = { viewModel.deleteSelectedBooks() }
            )

            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                tabs = {
                    tabLabels.forEachIndexed { index, type ->
                        Tab(
                            selected = index == pagerState.currentPage,
                            onClick = { scope.launch { pagerState.scrollToPage(index) } },
                            content = {
                                Text(
                                    text = type,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(16.dp)
                                )
                            },
                        )
                    }
                }
            )

            HorizontalPager(
                state = pagerState,
                pageContent = { tabIndex ->
                    val booksFlow by remember(tabIndex, sorting, statusFilter) {
                        derivedStateOf {
                            viewModel.getBooks(
                                filter = tabIndex,
                                sorting = sorting,
                                status = statusFilter
                            )
                        }
                    }
                    val books by booksFlow.collectAsState(emptyList())
                    val selectedBooks = viewModel.selectedBooks
                    val onClick: (id: Int) -> Unit = { id ->
                        if (selectedBooks.isNotEmpty() && !viewModel.isBookSelected(id)) { // in item selection mode
                            viewModel.addToSelectedBooks(id)
                        } else {
                            if (viewModel.isBookSelected(id)) {
                                viewModel.removeFromSelectedBooks(id)
                            } else {
                                onItemClick(id)
                            }
                        }
                    }

                    when (listViewMode) {
                        BookViewMode.LIST_VIEW -> {
                            BookListView(
                                books = books,
                                selectedBookIds = selectedBooks,
                                covers = covers,
                                onItemClick = onClick,
                                onItemLongClick = { id -> viewModel.addToSelectedBooks(id) },
                                onItemDelete = { id -> viewModel.deleteBook(id) },
                                onItemEdit = onItemEdit,
                                modifier = Modifier
                            )
                        }

                        BookViewMode.GRID_VIEW -> {
                            BookGridView(
                                books = books,
                                selectedBookIds = selectedBooks,
                                covers = covers,
                                onItemClick = onClick,
                                onItemLongClick = { id -> viewModel.addToSelectedBooks(id) },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            )
        }
    )

    BooklistBottomSheet(
        visible = isSearching,
        onDismissSearchSheet = onDismissSearching,
        onSearch = { query ->
            viewModel.searchFor(text = query, currentType = pagerState.currentPage)
        },
        covers = covers,
        onDeleteBook = { id -> viewModel.deleteBook(id) },
        onItemClick = onItemClick,
        onEditBook = onItemEdit
    )
}