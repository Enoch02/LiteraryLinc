package com.enoch02.booklist.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.booklist.R
import com.enoch02.database.model.Book
import com.enoch02.resources.LLString
import com.enoch02.resources.composables.SelectionOverlay
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookListView(
    books: List<Book>,
    selectedBookIds: List<Int>,
    covers: Map<String, String?>,
    onItemClick: (id: Int) -> Unit,
    onItemLongClick: (Int) -> Unit,
    onItemDelete: (id: Int) -> Unit,
    onItemEdit: (id: Int) -> Unit,
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
        val listState1 = rememberLazyListState()
        val state = rememberScrollAreaState(listState1)

        ScrollArea(
            state = state,
            modifier = modifier,
            content = {
                LazyColumn(
                    content = {
                        items(
                            count = books.size,
                            key = { index -> books[index].id!! },
                            itemContent = { index ->
                                val book = books[index]

                                SelectionOverlay(
                                    selected = selectedBookIds.contains(book.id),
                                    content = {
                                        BookListItem(
                                            modifier = Modifier
                                                .animateItem()
                                                .combinedClickable(
                                                    onClick = { book.id?.let { onItemClick(it) } },
                                                    onLongClick = {
                                                        book.id?.let { onItemLongClick(it) }
                                                    }
                                                ),
                                            book = book,
                                            coverPath = covers[book.coverImageName],
                                            onDelete = { book.id?.let { it1 -> onItemDelete(it1) } },
                                            onEdit = { book.id?.let { onItemEdit(it) } }
                                        )
                                    },
                                    modifier = Modifier.padding(1.dp)
                                )
                            }
                        )
                    },
                    state = listState1,
                    modifier = Modifier.fillMaxSize()
                )

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .width(8.dp),
                    thumb = {
                        Thumb(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.LightGray),
                            thumbVisibility = ThumbVisibility.HideWhileIdle(
                                enter = fadeIn(),
                                exit = fadeOut(),
                                hideDelay = 0.5.seconds
                            )
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun BookListItem(
    modifier: Modifier,
    book: Book,
    coverPath: String?,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var currentProgress by rememberSaveable { mutableFloatStateOf(0f) }
    val showEmptyProgress by remember { derivedStateOf { book.pagesRead == 0 && book.pageCount == 0 } }
    var isComplete by remember { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(book) {
        if (book.pagesRead != 0 && book.pageCount != 0) {
            currentProgress = (book.pagesRead.toFloat() / book.pageCount.toFloat()) * 100
        }
        isComplete = book.pagesRead == book.pageCount && book.pagesRead > 0
    }

    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val authorAlpha = if (book.author.isNotEmpty()) 1f else 0f
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(authorAlpha)
                )
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
                        progress = { currentProgress / 100f },
                        modifier = Modifier.height(10.dp),
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (showEmptyProgress) {
                    Text(
                        text = stringResource(LLString.progressNotRecorded),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Progress: ${currentProgress.toInt()}%",
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
                    shape = RoundedCornerShape(8.dp),
                    content = {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = null)
                    }
                )

                OutlinedIconButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Edit the entry for ${book.title}"
                        )
                    },
                )
            }
        },
        modifier = modifier
    )

    if (showWarningDialog) {
        ItemWarningDialog(
            onConfirm = {
                onDelete()
                showWarningDialog = false
            },
            onDismiss = {
                showWarningDialog = false
            },
            message = stringResource(LLString.deleteEntryWarning)
        )
    }
}

@Composable
private fun ItemWarningDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(LLString.warning),
    message: String
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                content = {
                    Text(text = stringResource(LLString.yes), color = Color.Red)
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text(text = stringResource(LLString.no))
                }
            )
        },
        title = {
            Text(title)
        },
        text = {
            Text(text = message)
        }
    )
}

@Preview
@Composable
private fun Preview() {
    Column {
        BookListItem(
            modifier = Modifier,
            book = Book(
                title = "hello, world!",
                author = "me",
                documentMd5 = "akjfavksklankn73g91he",
                pagesRead = 10,
                pageCount = 100
            ),
            coverPath = null,
            onDelete = {},
            onEdit = {}
        )

        BookListItem(
            modifier = Modifier,
            book = Book(
                title = "hello, world!",
                author = "you",
                documentMd5 = "",
                pagesRead = 100,
                pageCount = 100
            ),
            coverPath = null,
            onDelete = {},
            onEdit = {}
        )
    }
}
