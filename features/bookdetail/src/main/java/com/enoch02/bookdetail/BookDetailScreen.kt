package com.enoch02.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.enoch02.bookdetail.components.BookInfoText
import com.enoch02.bookdetail.components.DocumentsBottomSheet
import com.enoch02.bookdetail.components.WarningDialog
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
    val book = viewModel.book
    var showBookDetails by rememberSaveable { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable {
        mutableStateOf(false)
    }
    var showUnlinkWarningDialog by rememberSaveable { mutableStateOf(false) }
    var showDocumentSelectionModal by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        viewModel.getBookInfo(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(LLString.bookDetail),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
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
                        listOf(Icons.Rounded.Info, Icons.Rounded.Edit, Icons.Rounded.Delete)

                    icons.forEachIndexed { index, icon ->
                        IconButton(
                            onClick = {
                                when (index) {
                                    0 -> {
                                        showBookDetails = true
                                    }

                                    1 -> {
                                        navController.navigate(editScreenRoute())
                                    }

                                    2 -> {
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
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(8.dp),
                            content = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    content = {
                                        AsyncImage(
                                            model = viewModel.coverPath
                                                ?: R.drawable.placeholder_image,
                                            contentDescription = null,
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier
                                                .size(width = 149.dp, height = 225.dp)
                                                .align(Alignment.Center)
                                                .clip(RoundedCornerShape(8.dp)),
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                        fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = " by ${book.author}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                    content = {
                                        BookInfo(
                                            label = stringResource(LLString.rating),
                                            value = "${book.personalRating}/10"
                                        )
                                        BookInfo(
                                            label = stringResource(LLString.status),
                                            value = book.status
                                        )
                                        BookInfo(
                                            label = stringResource(LLString.pages),
                                            value = "${book.pageCount}"
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        )
                    }

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
                )
            }

            if (showWarningDialog) {
                AlertDialog(
                    onDismissRequest = { showWarningDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.deleteBook(id)
                                navController.popBackStack()
                            },
                            content = {
                                Text(text = stringResource(LLString.yes))
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
}

@Composable
fun BookInfo(label: String, value: String) {
    Column {
        Text(text = label, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
