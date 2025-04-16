package com.enoch02.bookdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.enoch02.bookdetail.components.BasicInfoView
import com.enoch02.bookdetail.components.DocumentsBottomSheet
import com.enoch02.bookdetail.components.OtherBookInfoView
import com.enoch02.bookdetail.components.QuickActionView
import com.enoch02.bookdetail.components.WarningDialog
import com.enoch02.database.model.Book
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.resources.LLString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    id: Int,
    editScreenRoute: () -> String,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val nullableBook by viewModel.getBookWith(id).collectAsState(Book())
    var showWarningDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showUnlinkWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showDocumentSelectionModal by remember { mutableStateOf(false) }

    if (nullableBook != null) {
        val book = nullableBook!!

        LaunchedEffect(key1 = book) {
            viewModel.getCover(book.coverImageName)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            content = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(LLString.navigateBack),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        )
                    },
                    actions = {
                        val icons =
                            listOf(Icons.Rounded.Edit, Icons.Rounded.Delete)

                        icons.forEachIndexed { index, icon ->
                            IconButton(
                                onClick = {
                                    when (index) {
                                        0 -> {
                                            navController.navigate(editScreenRoute())
                                        }

                                        1 -> {
                                            showWarningDialog = true
                                        }
                                    }
                                },
                                content = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            )
                        }

                        IconButton(
                            onClick = {
                                if (book.documentMd5.isNullOrEmpty()) {
                                    showDocumentSelectionModal = true
                                } else {
                                    showUnlinkWarningDialog = true
                                }
                            },
                            content = {
                                Icon(
                                    imageVector = if (book.documentMd5.isNullOrEmpty()) {
                                        Icons.Rounded.Link
                                    } else {
                                        Icons.Rounded.LinkOff
                                    },
                                    contentDescription = if (book.documentMd5.isNullOrEmpty()) {
                                        stringResource(LLString.linkDocument)
                                    } else {
                                        stringResource(LLString.unLinkDocument)
                                    },
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            },
            content = { paddingValues ->
                LazyColumn(
                    content = {
                        item {
                            val viewHeight = 225.dp

                            Column {
                                Row {
                                    Card {
                                        AsyncImage(
                                            model = viewModel.coverPath
                                                ?: R.drawable.placeholder_image,
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .size(width = 149.dp, height = viewHeight)
                                                .padding(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                    item {
                        Column(
                            modifier = Modifier.padding(top = 24.dp, start = 8.dp, end = 8.dp),
                            content = {
                                Text(
                                    text = stringResource(LLString.additionalNotes),
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = book.notes.ifBlank { stringResource(LLString.nothingHere) },
                                    textAlign = TextAlign.Justify
                                )
                            }
                        )
                    }
                },
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            )

            if (showBookDetails) {
                ModalBottomSheet(
                    onDismissRequest = { showBookDetails = false },
                    content = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            content = {
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.title),
                                        value = book.title
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.author),
                                        value = book.author
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.pageCount),
                                        value = "${book.pageCount}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.dateStarted),
                                        value = formatEpochAsString(book.dateStarted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.dateCompleted),
                                        value = formatEpochAsString(book.dateCompleted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.timesReread),
                                        value = "${book.timesReread}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.personalRating),
                                        value = "${book.personalRating}/10"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.isbn),
                                        value = book.isbn
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(LLString.genre),
                                        value = book.genre
                                    )
                                }
                            }
                        )
                    }
                                    Column(
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.height(viewHeight)
                                    ) {
                                        BasicInfoView(
                                            title = book.title,
                                            author = book.author,
                                            pagesRead = book.pagesRead,
                                            pageCount = book.pageCount
                                        )

                                        QuickActionView(
                                            currentStatus = book.status,
                                            currentRating = book.personalRating,
                                            onBookStatusChange = { newStatus ->
                                                viewModel.updateBookStatus(book, newStatus)
                                            },
                                            onBookRatingChange = { newRating ->
                                                viewModel.updateBookRating(book, newRating)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Text(
                                "Additional Information",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.bookType),
                                value = book.type
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.dateStarted),
                                value = formatEpochAsString(book.dateStarted)
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.dateCompleted),
                                value = formatEpochAsString(book.dateCompleted)
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.timesReread),
                                value = "${book.timesReread}"
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.isbn),
                                value = book.isbn
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.genre),
                                value = book.genre
                            )
                        }

                        item {
                            OtherBookInfoView(
                                infoLabel = stringResource(LLString.additionalNotes),
                                value = book.notes
                            )
                        }
                    },
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(8.dp)
                        .fillMaxSize()
                )

                if (showWarningDialog) {
                    AlertDialog(
                        onDismissRequest = { showWarningDialog = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteBook(id)
                                    showWarningDialog = false
                                },
                                content = {
                                    Text(text = stringResource(LLString.yes), color = Color.Red)
                                }
                            )
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showWarningDialog = false },
                                content = {
                                    Text(text = stringResource(LLString.no))
                                }
                            )
                        },
                        icon = {
                            Icon(imageVector = Icons.Rounded.Warning, contentDescription = null)
                        },
                        text = {
                            Text(text = stringResource(LLString.deleteEntryWarning))
                        }
                    )
                }

                if (showUnlinkWarningDialog) {
                    WarningDialog(
                        onConfirm = {
                            viewModel.unlinkDocumentFromBook(book)
                            showUnlinkWarningDialog = false
                        },
                        onDismiss = {
                            showUnlinkWarningDialog = false
                        },
                        message = stringResource(LLString.unlinkWarning)
                    )
                }

                if (showDocumentSelectionModal) {
                    val covers = viewModel.getCovers()
                        .collectAsState(initial = emptyMap()).value
                    val documents = viewModel.documents.collectAsState(emptyList()).value

                    DocumentsBottomSheet(
                        documents = documents,
                        covers = covers,
                        onDismiss = { showDocumentSelectionModal = false },
                        onDocumentSelected = { documentId ->
                            showDocumentSelectionModal = false
                            viewModel.linkDocumentToBook(book, documentId)
                        }
                    )
                }
            }
        )
    } else {
        // Nothing to show, go back
        navController.popBackStack()
    }
}
