package com.enoch02.booklist.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.enoch02.database.model.LLDocument
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BookListView(
    books: List<Book>,
    covers: Map<String, String?>,
    documents: List<LLDocument>,
    onItemClick: (id: Int) -> Unit,
    onItemDelete: (id: Int) -> Unit,
    onUnlinkDocument: (book: Book) -> Unit,
    onLinkDocument: (documentId: String, book: Book) -> Unit,
    modifier: Modifier
) {
    var showDocumentModal by remember { mutableStateOf(false) }
    var currentlyLinking: Book? by remember { mutableStateOf(null) }

    if (books.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = {
                Text(
                    text = stringResource(R.string.empty_booklist_msg),
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
                            key = { it },
                            itemContent = { index ->
                                val book = books[index]

                                BookListItem(
                                    modifier = Modifier.animateItem(),
                                    book = book,
                                    coverPath = covers[book.coverImageName],
                                    onClick = { book.id?.let { onItemClick(it) } },
                                    onDelete = { book.id?.let { it1 -> onItemDelete(it1) } },
                                    onUnlinkDocument = { theBook -> onUnlinkDocument(theBook) },
                                    onLinkDocument = {
                                        currentlyLinking = book
                                        showDocumentModal = true
                                    }
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

        BookListBottomSheet(
            showSheet = showDocumentModal,
            documents = documents,
            covers = covers,
            onDismiss = { showDocumentModal = false },
            onDocumentSelected = { document ->
                showDocumentModal = false
                currentlyLinking?.let { onLinkDocument(document, it) }
                currentlyLinking = null
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
    onUnlinkDocument: (book: Book) -> Unit,
    onLinkDocument: () -> Unit
) {
    var currentProgress by rememberSaveable { mutableFloatStateOf(0f) }
    val showEmptyProgress by remember { derivedStateOf { book.pagesRead == 0 && book.pageCount == 0 } }
    var isComplete by remember { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showUnlinkWarningDialog by rememberSaveable { mutableStateOf(false) }

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
                    fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val authorAlpha = if (book.author.isNotEmpty()) 1f else 0f
                Text(
                    text = book.author,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
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
                        text = stringResource(R.string.progress_not_recorded),
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
                    onClick = {
                        if (book.documentMd5.isNullOrEmpty()) {
                            onLinkDocument()
                        } else {
                            showUnlinkWarningDialog = true
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    content = {
                        Icon(
                            imageVector = if (book.documentMd5.isNullOrEmpty()) {
                                Icons.Rounded.Link
                            } else {
                                Icons.Rounded.LinkOff
                            },
                            contentDescription = stringResource(R.string.unlink_document)
                        )
                    },
                )
            }
        },
        modifier = modifier.clickable { onClick() }
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
            message = stringResource(R.string.delete_entry_warning)
        )
    }

    if (showUnlinkWarningDialog) {
        ItemWarningDialog(
            onConfirm = {
                onUnlinkDocument(book)
                showUnlinkWarningDialog = false
            },
            onDismiss = {
                showUnlinkWarningDialog = false
            },
            message = stringResource(R.string.unlink_entry_warning)
        )
    }
}

@Composable
private fun ItemWarningDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.warning),
    message: String
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                content = {
                    Text(text = stringResource(R.string.yes), color = Color.Red)
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text(text = stringResource(R.string.no))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookListBottomSheet(
    showSheet: Boolean,
    documents: List<LLDocument>,
    covers: Map<String, String?>,
    onDismiss: () -> Unit,
    onDocumentSelected: (documentId: String) -> Unit
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            content = {
                Card(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    content = {
                        LazyColumn(
                            content = {
                                items(
                                    items = documents,
                                    itemContent = { document ->
                                        ListItem(
                                            leadingContent = {
                                                AsyncImage(
                                                    model = covers[document.cover],
                                                    contentDescription = null,
                                                    modifier = Modifier.size(50.dp)
                                                )
                                            },
                                            headlineContent = {
                                                Text(text = document.name)
                                            },
                                            modifier = Modifier.clickable {
                                                onDocumentSelected(document.id)
                                            }
                                        )

                                        if (documents.indexOf(document) != documents.lastIndex) {
                                            HorizontalDivider()
                                        }
                                    }
                                )
                            }
                        )
                    })
            }
        )
    }
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
            onClick = {},
            onDelete = {},
            onUnlinkDocument = {},
            onLinkDocument = {}
        )

        BookListItem(
            modifier = Modifier,
            book = Book(
                title = "hello, world!",
                author = "you",
                documentMd5 = "",
                pagesRead = 10,
                pageCount = 100
            ),
            coverPath = null,
            onClick = {},
            onDelete = {},
            onUnlinkDocument = {},
            onLinkDocument = {}
        )
    }
}
