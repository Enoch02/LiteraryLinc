package com.enoch02.booklist.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
    onItemClick: (Int) -> Unit,
    onItemDelete: (Int) -> Unit
) {
    if (books.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 113.dp),
            verticalArrangement = Arrangement.Top,
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
            }
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

                    // TODO: change text color based on cover image color
                    Text(
                        text = book.title,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    )
}

@Preview
@Composable
fun GridItemPreview() {
    BookGridItem(
        modifier = Modifier,
        book = Book(title = "Hello World", author = "John Smith", pagesRead = 100, pageCount = 500),
        coverPath = "",
        onClick = { /*TODO*/ },
        onDelete = {

        }
    )
}
