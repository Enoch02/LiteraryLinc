package com.enoch02.booklist.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.booklist.R
import com.enoch02.database.model.Book

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookGridView(
    books: List<Book>,
    covers: Map<String, String?>,
    gridState: LazyGridState,
    onItemClick: (Int) -> Unit,
    onItemDelete: (Int) -> Unit,
    modifier: Modifier
) {
    //TODO: Extract string resource
    //TODO: implement hold to delete
    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = "Your book list is empty.\nTap the + button to start tracking",
                    textAlign = TextAlign.Center
                )
            }
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 113.dp),
            verticalArrangement = Arrangement.Top,
            state = gridState,
            content = {
                items(
                    count = books.size,
                    itemContent = { index ->
                        val book = books[index]

                        BookGridItem(
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            book = book,
                            coverPath = covers[book.coverImageName],
                            onClick = { book.id?.let { onItemClick(it) } },
                            onDelete = { book.id?.let { it1 -> onItemDelete(it1) } }
                        )
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        )
    }
}

/**
 * Most [Book] properties are not used. Might remove it.
 * Implement onHold for deleting and multi-selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookGridItem(
    modifier: Modifier,
    book: Book,
    coverPath: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .size(width = 149.dp, height = 225.dp),
        onClick = onClick,
        content = {
            Box(
                contentAlignment = Alignment.BottomStart,
                content = {
                    AsyncImage(
                        //TODO: replace placeholder with proper default image for book covers
                        model = coverPath ?: R.drawable.placeholder_image,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .fillMaxWidth(),
                        content = {
                            Text(
                                text = book.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(4.dp)
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
                        coverPath = "",
                        onClick = { /*TODO*/ },
                        onDelete = {

                        }
                    )
                }
            }
        }
    )
}
