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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.enoch02.components.BookInfoText
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
    /*val bookSaver = Saver<Book, Map<Int, Any?>>(
        save = {
            mapOf(0 to it.id, 1 to it.title, 2 to it.type, 3 to it.coverImageName)
        },
        restore = {
            Book(
                id = it[0] as Int,
                title = it[1].toString(),
                type = it[2].toString(),
                coverImageName = it[3].toString()
            )
        }
    )*/
    /*var book by rememberSaveable(saver = bookSaver) { mutableStateOf(Book()) }*/
    var book by remember { mutableStateOf(Book()) }
    var coverPath: String? by rememberSaveable { mutableStateOf(null) }
    var showBookDetails by rememberSaveable { mutableStateOf(false) }

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
                                imageVector = Icons.Rounded.ArrowBack,
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
                                        viewModel.deleteBook(id)
                                        navController.popBackStack()
                                    }
                                }
                            },
                            content = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null/*TODO*/,
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
                                            model = coverPath,
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = book.title,
                                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                        fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Justify
                                    )
                                    Text(
                                        text = " by ${book.author}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        textAlign = TextAlign.Justify
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
                                        BookInfo(label = "Status", value = book.status)
                                        BookInfo(label = "Pages", value = "${book.pageCount}")
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
                                //TODO: extract strings
                                Text(
                                    text = "Additional Notes",
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                    fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = book.notes.ifBlank { "There's nothing here... 🥲" },
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

            //TODO: add date started and date completed
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
                                item { BookInfoText(header = "Title", value = book.title) }
                                item { BookInfoText(header = "Author", value = book.author) }
                                item {
                                    BookInfoText(
                                        header = "Page count",
                                        value = "${book.pageCount}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = "Date Started",
                                        value = formatEpochAsString(book.dateStarted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = "Date Completed",
                                        value = formatEpochAsString(book.dateCompleted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = "Times Reread",
                                        value = "${book.timesReread}"
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = "Personal Rating",
                                        value = "${book.personalRating}/10"
                                    )
                                }
                                item { BookInfoText(header = "ISBN", value = book.isbn) }
                                item { BookInfoText(header = "Genre(s)", value = book.genre) }
                            }
                        )
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
