package com.enoch02.booklist.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.booklist.R
import com.enoch02.database.model.Book

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookListView(
    books: List<Book>,
    covers: Map<String, String?>,
    listState: LazyListState,
    onItemClick: (id: Int) -> Unit,
    onItemDelete: (id: Int) -> Unit,
    onItemIncrement: (id: Int) -> Unit
) {
    LazyColumn(
        content = {
            items(
                count = books.size,
                itemContent = { index ->
                    val book = books[index]

                    BookListItem(
                        modifier = Modifier.animateItemPlacement(),
                        book = book,
                        coverPath = covers[book.coverImageName],
                        onClick = { book.id?.let { onItemClick(it) } },
                        onDelete = { book.id?.let { it1 -> onItemDelete(it1) } },
                        onItemIncrement = { book.id?.let { onItemIncrement(it) } }
                    )
                }
            )
        },
        state = listState,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun BookListItem(
    modifier: Modifier,
    book: Book,
    coverPath: String?,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onItemIncrement: () -> Unit
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    val currentPercentage by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )
    val showEmptyProgress by remember { derivedStateOf { book.pagesRead == 0 && book.pageCount == 0 } }
    var isComplete by remember { mutableStateOf(false) }

    LaunchedEffect(
        key1 = book,
        block = {
            if (book.pagesRead != 0 && book.pageCount != 0) {
                currentProgress = (book.pagesRead.toFloat() / book.pageCount.toFloat()) * 100
            }
            isComplete =
                book.pagesRead == book.pageCount && book.pagesRead != 0 && book.pageCount != 0
        }
    )

    ListItem(
        headlineContent = {
            Text(
                text = book.title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                maxLines = 4,
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
            Spacer(modifier = Modifier.height(4.dp))

            if (showEmptyProgress) {
                Text(text = "Progress: ?%")
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
                modifier = Modifier
                    .size(width = 50.dp, height = 80.dp)
            )
        },
        supportingContent = {
            Box(modifier = Modifier.height(45.dp))
        },
        trailingContent = {
            //TODO: Replace with painterResource
            Column {
                OutlinedIconButton(
                    onClick = { onDelete() },
                    shape = RectangleShape,
                    content = {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                    }
                )

                OutlinedIconButton(
                    onClick = { onItemIncrement() },
                    shape = RectangleShape,
                    content = {
                        Icon(
                            imageVector = if (isComplete) Icons.Rounded.Check else Icons.Rounded.PlusOne,
                            contentDescription = null
                        )
                    },
                    enabled = !isComplete
                )
            }
        },
        modifier = modifier.clickable { onClick() }
    )
}