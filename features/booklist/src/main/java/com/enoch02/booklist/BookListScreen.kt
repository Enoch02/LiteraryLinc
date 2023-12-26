package com.enoch02.booklist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalAnimationApi::class)
@Composable
fun BookListScreen(
    modifier: Modifier,
    scope: CoroutineScope,
    sorting: Sorting,
    listViewMode: BookListViewMode,
    listState: LazyListState,
    gridState: LazyGridState,
    onItemClick: (Int) -> Unit,
    viewModel: BookListViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState()
    val tabLabels = Book.types.values

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            tabs = {
                tabLabels.forEachIndexed { index, type ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        content = {
                            Text(
                                text = type,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    )
                }
            }
        )

        HorizontalPager(
            count = tabLabels.size,
            state = pagerState,
            content = { tabIndex ->
                val books = viewModel.getBooks(tabIndex, sorting)
                    .collectAsState(initial = emptyList()).value
                val covers = viewModel.getCovers()
                    .collectAsState(initial = emptyMap()).value

                AnimatedContent(
                    targetState = listViewMode,
                    label = "book list animation",
                    content = { lvm ->
                        when (lvm) {
                            BookListViewMode.LIST_VIEW -> {
                                BookListView(
                                    books = books,
                                    covers = covers,
                                    onItemClick = onItemClick,
                                    listState = listState,
                                    onItemDelete = { id ->
                                        viewModel.deleteBook(id)
                                    },
                                    onItemIncrement = { id ->
                                        //TODO: Might remove
                                        //TODO: Increment works but the view is not updated immediately
                                        viewModel.incrementBook(id)
                                    }
                                )
                            }

                            BookListViewMode.GRID_VIEW -> {
                                BookGridView(
                                    books = books,
                                    covers = covers,
                                    gridState = gridState,
                                    onItemClick = onItemClick,
                                    onItemDelete = { id -> viewModel.deleteBook(id) }
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}