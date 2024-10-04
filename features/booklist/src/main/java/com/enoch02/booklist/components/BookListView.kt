package com.enoch02.booklist.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
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
    val state = rememberScrollAreaState(listState)

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
        ScrollArea(
            state = state,
            content = {
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

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .width(4.dp),
                    thumb = {
                        Thumb(Modifier.background(Color.LightGray))
                    }
                )
            }
        )
    }
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
    var currentProgress by rememberSaveable { mutableFloatStateOf(0f) }

    val currentPercentage by animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress"
    )

    val showEmptyProgress by remember { derivedStateOf { book.pagesRead == 0 && book.pageCount == 0 } }
    var isComplete by remember { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(book) {
        if (book.pagesRead != 0 && book.pageCount != 0) {
            currentProgress = (book.pagesRead.toFloat() / book.pageCount.toFloat()) * 100
        }
        isComplete =
            book.pagesRead == book.pageCount && book.pagesRead > 0 && book.pageCount > 0
    }

    ListItem(
        headlineContent = {
            Column {
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
            }
        },
        leadingContent = {
            AsyncImage(
                model = coverPath ?: R.drawable.placeholder_image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .size(width = 50.dp, height = 80.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        },
        supportingContent = {
            Column {
                if (showEmptyProgress) {
                    LinearProgressIndicator(
                        progress = { 0f },
                        modifier = Modifier.height(10.dp),
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { currentPercentage / 100f },
                        modifier = Modifier.height(10.dp),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (showEmptyProgress) {
                    Text(
                        text = "Progress: Not Recorded",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Progress: ${currentPercentage.toInt()}%",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

            }
        },
        trailingContent = {
            Column {
                OutlinedIconButton(
                    onClick = { showWarningDialog = true },
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

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { onDelete() },
                    content = {
                        Text(text = "Yes")
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showWarningDialog = false },
                    content = {
                        Text(text = "No")
                    }
                )
            },
            /*icon = {
                Icon(imageVector = Icons.Rounded.Warning, contentDescription = null)
            },*/
            text = {
                Text(text = "Do you want to delete this entry?", textAlign = TextAlign.Center)
            }
        )
    }
}
