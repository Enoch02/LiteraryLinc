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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.booklist.components.BookGridView
import com.enoch02.booklist.components.BookListView
import com.enoch02.booklist.components.BookViewMode
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
    viewModel: BookListViewModel = hiltViewModel()
) {
    val tabLabels = Book.Companion.BookType.entries.map { it.strName }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabLabels.size })

    Column(
        modifier = modifier.fillMaxSize(),
        content = {
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
                    val covers = viewModel.getCovers()
                        .collectAsState(initial = emptyMap()).value

                    when (listViewMode) {
                        BookViewMode.LIST_VIEW -> {
                            BookListView(
                                books = books,
                                covers = covers,
                                onItemClick = onItemClick,
                                onItemDelete = { id ->
                                    viewModel.deleteBook(id)
                                },
                                onItemEdit = onItemEdit,
                                modifier = Modifier
                            )
                        }

                        BookViewMode.GRID_VIEW -> {
                            BookGridView(
                                books = books,
                                covers = covers,
                                onItemClick = onItemClick,
                                onItemDelete = { id -> viewModel.deleteBook(id) },
                                modifier = Modifier
                            )
                        }
                    }
                }
            )
        }
    )
}