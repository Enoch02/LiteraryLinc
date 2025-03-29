package com.enoch02.booklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    var showDeletionConfirmation by remember { mutableStateOf(false) }
    val covers = viewModel.getCovers()
        .collectAsState(initial = emptyMap()).value

    Column(
        modifier = modifier.fillMaxSize(),
        content = {
            AnimatedVisibility(
                visible = viewModel.selectedBooks.isNotEmpty(),
                content = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                IconButton(
                                    onClick = { viewModel.clearSelectedBooks() },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.Clear,
                                            contentDescription = stringResource(R.string.clear_selection_desc)
                                        )
                                    }
                                )

                                Text(
                                    text = "${viewModel.selectedBooks.size}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { viewModel.selectAllBooks() },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.SelectAll,
                                            contentDescription = stringResource(R.string.select_all_desc)
                                        )
                                    }
                                )

                                IconButton(
                                    onClick = { viewModel.invertSelection() },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.FlipToBack,
                                            contentDescription = stringResource(R.string.invert_selection_desc)
                                        )
                                    }
                                )


                                IconButton(
                                    onClick = { showDeletionConfirmation = true },
                                    content = {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = stringResource(R.string.delete_selection_desc)
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
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
                    val books = viewModel.getBooks(
                        filter = tabIndex, sorting = sorting,
                        status = statusFilter
                    )
                        .collectAsState(initial = emptyList()).value
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
                                onItemDelete = { id -> viewModel.deleteBook(id) },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            )
        }
    )

    ConfirmDeletionDialog(
        visible = showDeletionConfirmation,
        onDismiss = { showDeletionConfirmation = false },
        onConfirm = {
            viewModel.deleteSelectedBooks()
            showDeletionConfirmation = false
        },
        message = stringResource(
            R.string.book_list_multi_deletion_warning,
            viewModel.selectedBooks.size
        )
    )

    BooklistBottomSheet(
        visible = isSearching,
        onDismissSearchSheet = onDismissSearching,
        onSearch = { query ->
            viewModel.searchFor(query)
        },
        covers = covers,
        onDeleteBook = { id -> viewModel.deleteBook(id) },
        onItemClick = onItemClick,
        onEditBook = onItemEdit
    )
}

@Composable
private fun ConfirmDeletionDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(R.string.warning)) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    content = { Text(text = stringResource(R.string.yes), color = Color.Red) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = { Text(text = stringResource(R.string.no)) }
                )
            }
        )
    }
}