package com.enoch02.bookdetail

import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.components.BookDetailHeader
import com.enoch02.components.BookInfoText
import com.enoch02.database.model.Book
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
                title = { Text(text = stringResource(R.string.book_detail_label)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = stringResource(
                                R.string.navigate_up_desc
                            )
                        )
                    }
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
                                Icon(imageVector = icon, contentDescription = null/*TODO*/)
                            }
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp)
                    .fillMaxSize()
            ) {
                item {
                    BookDetailHeader(
                        coverPath = coverPath.toString(),
                        title = book.title,
                        author = book.author,
                        publicationDate = "",
                        genre = book.genre,
                        status = book.status,
                        modifier = Modifier
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text(text = book.description)
                }
            }

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
                                        value = formatEpochDate(book.dateStarted)
                                    )
                                }
                                item {
                                    BookInfoText(
                                        header = "Date Completed",
                                        value = formatEpochDate(book.dateCompleted)
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

//TODO: move to some module or package...
fun formatEpochDate(date: Long?): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && date != null -> {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            val dateObj = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(date),
                ZoneId.systemDefault()
            )
            formatter.format(dateObj)
        }

        Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && date != null -> {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
            formatter.format(Date(date))
        }

        else -> {
            ""
        }
    }
}
