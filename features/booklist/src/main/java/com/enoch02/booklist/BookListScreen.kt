package com.enoch02.booklist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enoch02.database.model.Book
import com.enoch02.database.model.BookType
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun BookListScreen(modifier: Modifier, scope: CoroutineScope) {
    val pagerState = rememberPagerState()
    val mediaType = listOf("All", "Books", "Manga/LN", "Comics")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            tabs = {
                mediaType.forEachIndexed { index, medium ->
                    Tab(
                        selected = index == pagerState.currentPage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        content = {
                            Text(
                                text = medium,
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
            count = mediaType.size,
            state = pagerState,
            content = {
                BookListView(filter = it)
            }
        )
    }
}

@Composable
private fun BookListView(filter: Int, viewModel: BookListViewModel = viewModel()) {
    val books = emptyList<Book>()

    if (books.isNotEmpty()) {
        LazyColumn(
            content = {
                items(
                    count = books.size,
                    itemContent = { index ->
                        val dummyItem = books[index]

                        Item(
                            title = dummyItem.title,
                            authors = dummyItem.authors,
                            type = dummyItem.type,
                            onClick = {})
                        /*if (index != dummyItems.size - 1) {
                            Divider()
                        }*/
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
private fun Item(title: String, authors: List<String>, onClick: () -> Unit, type: BookType) {
    ListItem(
        overlineContent = {
            Text(text = type.name.replace("_", " or "))
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
            Image(
                painter = painterResource(R.drawable.placeholder_image),
                contentDescription = "Dummy",
                modifier = Modifier.height(60.dp)
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
    BookListScreen(modifier = Modifier, rememberCoroutineScope())
}