package com.enoch02.booklist.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.booklist.R
import com.enoch02.database.model.Book
import com.enoch02.resources.LLString
import com.enoch02.resources.composables.SelectionOverlay

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookGridView(
    books: List<Book>,
    selectedBookIds: List<Int>,
    covers: Map<String, String?>,
    onItemClick: (Int) -> Unit,
    onItemLongClick: (Int) -> Unit,
    modifier: Modifier
) {
    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = stringResource(LLString.emptyBooklistMsg),
                    textAlign = TextAlign.Center
                )
            }
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 113.dp),
            verticalArrangement = Arrangement.Top,
            state = rememberLazyGridState(),
            content = {
                items(
                    count = books.size,
                    key = { index -> books[index].id!! },
                    itemContent = { index ->
                        val book = books[index]

                        SelectionOverlay(
                            selected = selectedBookIds.contains(book.id),
                            content = {
                                BookGridItem(
                                    modifier = Modifier
                                        .combinedClickable(
                                            onClick = { book.id?.let { onItemClick(it) } },
                                            onLongClick = { book.id?.let { onItemLongClick(it) } }
                                        )
                                        .animateItem()
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                    book = book,
                                    coverPath = covers[book.coverImageName],
                                )
                            },
                            modifier = Modifier.padding(1.dp)
                        )
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun BookGridItem(
    modifier: Modifier,
    book: Book,
    coverPath: String?
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .size(width = 150.dp, height = 200.dp),
        content = {
            Box(
                contentAlignment = Alignment.BottomStart,
                content = {
                    AsyncImage(
                        model = coverPath ?: R.drawable.placeholder_image,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.primary
                                    )
                                )
                            )
                            .fillMaxHeight(0.2f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomStart,
                        content = {
                            Text(
                                text = book.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier
                                    .padding(4.dp)
                            )
                        }
                    )
                }
            )
        }
    )
}

@Preview
@Composable
fun GridItemPreview() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(113.dp),
        content = {
            repeat(5) {
                item {
                    BookGridItem(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                        book = Book(
                            title = "Hello World $it",
                            author = "John Smith",
                            pagesRead = 100,
                            pageCount = 500
                        ),
                        coverPath = ""
                    )
                }
            }
        }
    )
}
