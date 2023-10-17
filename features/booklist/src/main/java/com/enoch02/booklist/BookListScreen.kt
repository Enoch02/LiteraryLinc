package com.enoch02.booklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.enoch02.database.model.Book
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BookListScreen(
    modifier: Modifier,
    scope: CoroutineScope,
    onItemClick: () -> Unit,
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
                BookListView(
                    books = viewModel.getBooks(tabIndex)
                        .collectAsState(initial = emptyList()).value,
                    covers = viewModel.getCovers().collectAsState(initial = emptyMap()).value,
                    onItemClick = onItemClick
                )
            }
        )
    }
}

@Composable
private fun BookListView(
    books: List<Book>,
    covers: Map<String, String?>,
    onItemClick: () -> Unit
) {

    if (books.isNotEmpty()) {
        LazyColumn(
            content = {
                items(
                    count = books.size,
                    itemContent = { index ->
                        val book = books[index]

                        Item(
                            title = book.title,
                            authors = emptyList() /*TODO:*/ /*dummyItem.authors*/,
                            type = book.type,
                            coverPath = covers[book.coverImageName],
                            onClick = onItemClick
                        )
                    }
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Your list is empty!", fontWeight = FontWeight.Bold)
            Text(text = "Add Some Books to start tracking them.")
        }
    }
}

@Composable
private fun Item(
    title: String,
    authors: List<String>,
    coverPath: String?,
    onClick: () -> Unit,
    type: String
) {
    ListItem(
        overlineContent = {
            Text(text = type)
        },
        headlineContent = {
            Text(
                text = title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
        },
        leadingContent = {
            AsyncImage(
                //TODO: replace with proper default image for book covers
                model = coverPath ?: R.drawable.placeholder_image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(width = 80.dp, height = 120.dp),
            )
        },
        supportingContent = {
            Text(
                text = if (authors.size > 5) {
                    authors.slice(0..5).joinToString(", ")
                } else {
                    authors.joinToString(", ")
                }
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun Preview() {
    BookListScreen(modifier = Modifier, scope = rememberCoroutineScope(), onItemClick = {})
}