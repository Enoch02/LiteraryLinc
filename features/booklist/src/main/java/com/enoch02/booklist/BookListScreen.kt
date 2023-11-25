package com.enoch02.booklist

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.enoch02.database.model.Book
import com.enoch02.database.model.Sorting
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
    sorting: Sorting,
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
                BookListView(
                    books = viewModel.getBooks(tabIndex, sorting)
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
    onItemClick: (Int) -> Unit
) {

    if (books.isNotEmpty()) {
        LazyColumn(
            content = {
                items(
                    count = books.size,
                    itemContent = { index ->
                        val book = books[index]

                        Item(
                            book = book,
                            coverPath = covers[book.coverImageName],
                            onClick = { book.id?.let { onItemClick(it) } }
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
    book: Book,
    coverPath: String?,
    onClick: () -> Unit,
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    val currentPercentage by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )
    var showEmptyProgress by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(
        key1 = Unit,
        block = {
            if (book.pagesRead != 0 && book.pageCount != 0) {
                currentProgress = (book.pagesRead.toFloat() / book.pageCount.toFloat()) * 100
            } else {
                showEmptyProgress = true
            }
        }
    )

    ListItem(
        headlineContent = {
            Text(
                text = book.title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (book.author.isNotEmpty()) {
                Text(
                    text = "by ${book.author}",
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (showEmptyProgress) {
                LinearProgressIndicator(progress = 0.5f, modifier = Modifier.height(10.dp))
            } else {
                LinearProgressIndicator(
                    progress = currentPercentage / 100f,
                    modifier = Modifier
                        .height(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))

            if (showEmptyProgress) {
                Text(text = "Progress: ??")
            } else {
                Text(text = "Progress: ${currentPercentage.toInt()}%")
            }
        },
        leadingContent = {
            AsyncImage(
                //TODO: replace placeholder with proper default image for book covers
                model = coverPath ?: R.drawable.placeholder_image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(width = 50.dp, height = 80.dp),
            )
        },
        supportingContent = {
            Box(modifier = Modifier.height(45.dp))
        },
        modifier = Modifier.clickable { onClick() }
    )
}
