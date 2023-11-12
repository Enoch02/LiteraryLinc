package com.enoch02

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.addbook.R
import com.enoch02.composables.BackArrowButton
import com.enoch02.composables.FormIntField
import com.enoch02.composables.FormSlider
import com.enoch02.composables.FormSpinner
import com.enoch02.composables.FormTextField
import com.enoch02.composables.ImagePicker
import com.enoch02.composables.IncrementalFormIntField
import com.enoch02.database.model.Book
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    navController: NavController,
    id: Int,
    viewModel: ModifyBookViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var book by remember { mutableStateOf(Book()) }
    // For the cover currently stored in the app
    var coverPath: String? by rememberSaveable { mutableStateOf(null) }

    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(Book.types.values.first()) }
    var status by rememberSaveable { mutableStateOf(Book.status.first()) }
    var pagesRead by rememberSaveable { mutableStateOf("0") }
    var pageCount by rememberSaveable { mutableStateOf("0") }
    var timesReread by rememberSaveable { mutableStateOf("0") }
    var personalRating by rememberSaveable { mutableStateOf("0") }
    var isbn by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("") }
    var bookDescription by rememberSaveable { mutableStateOf("") }
    var coverImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    LaunchedEffect(key1 = Unit) {
        book = viewModel.getBook(id)
        if (!book.coverImageName.isNullOrEmpty()) {
            coverPath = viewModel.getCover(book.coverImageName!!)
        }

        title = book.title
        author = book.author
        type = book.type
        status = book.status
        pagesRead = book.pagesRead.toString()
        pageCount = book.pageCount.toString()
        timesReread = book.timesReread.toString()
        personalRating = book.personalRating.toString()
        isbn = book.isbn
        genre = book.genre
        bookDescription = book.description
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.editing_text, book.title)) },
                navigationIcon = {
                    BackArrowButton {
                        navController.popBackStack()
                    }
                }
            )
        },
        floatingActionButton = {
            val scope = rememberCoroutineScope()

            FloatingActionButton(
                onClick = {
                    scope.launch {
                        viewModel.updateBook(
                            id = id,
                            title = title,
                            author = author,
                            pagesRead = pagesRead,
                            pageCount = pageCount,
                            timesReread = timesReread,
                            personalRating = personalRating,
                            isbn = isbn,
                            genre = genre,
                            type = type,
                            coverImageUri = coverImageUri,
                            coverImageName = book.coverImageName,
                            description = bookDescription,
                            status = status
                        ).onSuccess {
                            navController.popBackStack()
                            navController.popBackStack()
                        }.onFailure { e ->
                            Toast.makeText(
                                context,
                                "${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_save_24),
                        contentDescription = stringResource(R.string.save_book_desc)
                    )
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp),
                content = {
                    item {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.Top,
                            content = {
                                ImagePicker(
                                    label = "Add Cover Image",
                                    launcher = rememberLauncherForActivityResult(
                                        ActivityResultContracts.StartActivityForResult()
                                    ) {
                                        coverImageUri = it.data?.data
                                    },
                                    coverImageUri = coverImageUri,
                                    coverImagePath = coverPath
                                )

                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        )
                    }
                    item {
                        FormTextField(
                            label = "Book Title",
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormTextField(
                            label = "Author",
                            value = author,
                            onValueChange = { author = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormSpinner(
                            label = stringResource(R.string.book_type_label),
                            options = Book.types.values.toList(),
                            selectedOption = type,
                            onSelectionChange = { type = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormSpinner(
                            label = stringResource(R.string.status_label),
                            options = Book.status,
                            selectedOption = status,
                            onSelectionChange = { status = it }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            content = {
                                FormIntField(
                                    label = stringResource(R.string.pages_read_label),
                                    value = pagesRead,
                                    onValueChange = { pagesRead = it },
                                    modifier = Modifier.weight(0.45f)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .weight(0.1f)
                                )
                                FormIntField(
                                    label = stringResource(R.string.page_count_label),
                                    value = pageCount,
                                    onValueChange = { pageCount = it },
                                    modifier = Modifier.weight(0.45f)
                                )
                            }
                        )
                    }

                    item {
                        IncrementalFormIntField(
                            label = stringResource(R.string.times_reread_label),
                            value = timesReread,
                            onValueChange = { timesReread = it },
                            onIncrement = { timesReread = it },
                            onDecrement = { timesReread = it }
                        )
                    }

                    item {
                        FormSlider(
                            label = stringResource(R.string.rating_label),
                            value = personalRating.toFloat(),
                            onValueChange = { personalRating = "${it.toInt()}" },
                            range = 0f..10f,
                            steps = 10,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormTextField(
                            label = stringResource(R.string.isbn_label),
                            value = isbn,
                            onValueChange = { isbn = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormTextField(
                            label = stringResource(R.string.genre_label),
                            value = genre,
                            onValueChange = { genre = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.description_label),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bookDescription,
                                onValueChange = { bookDescription = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 200.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Words,
                                )
                            )
                        }
                    }
                }
            )
        }
    )
}