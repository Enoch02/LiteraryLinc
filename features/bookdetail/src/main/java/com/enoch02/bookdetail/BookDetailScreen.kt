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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
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
import com.enoch02.database.model.Book
import com.enoch02.database.util.formatEpochAsString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    navController: NavController,
    id: Int,
    editScreenRoute: () -> String,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    var book by remember { mutableStateOf(Book()) }
    var coverPath: String? by rememberSaveable { mutableStateOf(null) }
    var showBookDetails by rememberSaveable { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        book = viewModel.getBook(id)
        if (!book.coverImageName.isNullOrEmpty()) {
            coverPath = viewModel.getCover(book.coverImageName!!)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.book_detail_label),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(
                                    R.string.navigate_up_desc
                                ),
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
                                            model = if (coverPath == null) {
                                                R.drawable.placeholder_image
                                            } else {
                                                coverPath
                                            },
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
                                            label = "Rating",
                                            value = "${book.personalRating}/10"
                                        )
                                        BookInfo(label = stringResource(R.string.status), value = book.status)
                                        BookInfo(label = stringResource(R.string.pages), value = "${book.pageCount}")
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
                                    text = stringResource(R.string.additional_notes),
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = book.notes.ifBlank { stringResource(R.string.nothing) },
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
                                        header = stringResource(R.string.title),
                                        value = book.title
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.author),
                                        value = book.author
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.page_count),
                                        value = "${book.pageCount}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.date_started),
                                        value = formatEpochAsString(book.dateStarted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.date_completed),
                                        value = formatEpochAsString(book.dateCompleted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.times_reread),
                                        value = "${book.timesReread}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.personal_rating),
                                        value = "${book.personalRating}/10"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.isbn),
                                        value = book.isbn
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = stringResource(R.string.genre),
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
                                Text(text = stringResource(R.string.yes))
                            }
                        )
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showWarningDialog = false },
                            content = {
                                Text(text = stringResource(R.string.no))
                            }
                        )
                    },
                    icon = {
                        Icon(imageVector = Icons.Rounded.Warning, contentDescription = null)
                    },
                    text = {
                        Text(text = stringResource(R.string.delete_entry_warning))
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
