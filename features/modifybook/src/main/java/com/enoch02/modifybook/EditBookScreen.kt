package com.enoch02.modifybook

import android.net.Uri
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import com.enoch02.components.BackArrowButton
import com.enoch02.components.FormDatePicker
import com.enoch02.components.FormIntField
import com.enoch02.components.FormSlider
import com.enoch02.components.FormSpinner
import com.enoch02.components.FormTextField
import com.enoch02.components.ImagePicker
import com.enoch02.components.IncrementalFormIntField
import com.enoch02.database.model.Book
import com.enoch02.database.model.Book.Companion.BookStatus
import com.enoch02.database.model.Book.Companion.BookType
import com.enoch02.resources.LLString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookScreen(
    navController: NavController,
    id: Int,
    viewModel: ModifyBookViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var book by remember { mutableStateOf(Book()) }
    // For the cover currently stored in the app
    var coverPath: String? by rememberSaveable { mutableStateOf(null) }

    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(BookType.ANY.strName) }
    var status by rememberSaveable { mutableStateOf(BookStatus.PLANNING.strName) }
    var pagesRead by rememberSaveable { mutableStateOf("0") }
    var pageCount by rememberSaveable { mutableStateOf("0") }
    val dateStarted = rememberDatePickerState(initialSelectedDateMillis = null)
    val dateCompleted = rememberDatePickerState(initialSelectedDateMillis = null)
    var timesReread by rememberSaveable { mutableStateOf("0") }
    var personalRating by rememberSaveable { mutableStateOf("0") }
    var isbn by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("") }
    var bookNotes by rememberSaveable { mutableStateOf("") }
    var coverImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var volumesRead by rememberSaveable { mutableStateOf("0") }
    var totalVolumes by rememberSaveable { mutableStateOf("0") }

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
        dateStarted.selectedDateMillis = book.dateStarted
        dateCompleted.selectedDateMillis = book.dateCompleted
        timesReread = book.timesReread.toString()
        personalRating = book.personalRating.toString()
        isbn = book.isbn
        genre = book.genre
        bookNotes = book.notes
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
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
                            dateStarted = dateStarted.selectedDateMillis,
                            dateCompleted = dateCompleted.selectedDateMillis,
                            timesReread = timesReread,
                            personalRating = personalRating,
                            isbn = isbn,
                            genre = genre,
                            type = type,
                            coverImageUri = coverImageUri,
                            coverImageName = book.coverImageName,
                            notes = bookNotes,
                            status = status,
                            volumesRead = volumesRead,
                            totalVolumes = totalVolumes,
                            documentMd5 = book.documentMd5
                        ).onSuccess {
                            navController.popBackStack()
                        }.onFailure { e ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("${e.message}")
                            }
                        }
                    }
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_save_24),
                        contentDescription = stringResource(LLString.save)
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
                                    label = stringResource(LLString.coverImagePickerLabel),
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
                            label = stringResource(LLString.bookTitle),
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormTextField(
                            label = stringResource(LLString.author),
                            value = author,
                            onValueChange = { author = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormSpinner(
                            label = stringResource(LLString.bookType),
                            options = Book.Companion.BookType.entries.map { it.strName },
                            selectedOption = type,
                            onSelectionChange = { type = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormSpinner(
                            label = stringResource(LLString.status),
                            options = BookStatus.entries.map { it.strName },
                            selectedOption = status,
                            onSelectionChange = { status = it }
                        )
                    }

                    item {
                        FormDatePicker(
                            label = stringResource(LLString.startDate),
                            datePickerState = dateStarted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        )
                    }

                    item {
                        FormDatePicker(
                            label = stringResource(LLString.completionDate),
                            datePickerState = dateCompleted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
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
                                    label = stringResource(LLString.pagesRead),
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
                                    label = stringResource(LLString.pageCount),
                                    value = pageCount,
                                    onValueChange = { pageCount = it },
                                    modifier = Modifier.weight(0.45f)
                                )

                                //INFO: disabling this aspect of manga and comic tracking for now
                                // till i come up with a better solution
                                /*FormIntField(
                                    label = stringResource(R.string.volumes_read_label),
                                    value = volumesRead,
                                    onValueChange = { volumesRead = it },
                                    modifier = Modifier.weight(0.45f)
                                )
                                Spacer(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .weight(0.1f)
                                )
                                FormIntField(
                                    label = stringResource(R.string.total_volumes_label),
                                    value = totalVolumes,
                                    onValueChange = { totalVolumes = it },
                                    modifier = Modifier.weight(0.45f)
                                )*/
                            }
                        )
                    }

                    item {
                        IncrementalFormIntField(
                            label = stringResource(LLString.timesReread),
                            value = timesReread,
                            onValueChange = { timesReread = it },
                            onIncrement = { timesReread = it },
                            onDecrement = { timesReread = it }
                        )
                    }

                    item {
                        FormSlider(
                            label = stringResource(LLString.rating),
                            value = personalRating.toFloat(),
                            onValueChange = { personalRating = "${it.toInt()}" },
                            range = 0f..10f,
                            steps = 9,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormIntField(
                            label = stringResource(LLString.isbn),
                            value = isbn,
                            onValueChange = { isbn = it },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        FormTextField(
                            label = stringResource(LLString.genre),
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
                                text = stringResource(LLString.additionalNotes),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bookNotes,
                                onValueChange = { bookNotes = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 200.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next,
                                    capitalization = KeyboardCapitalization.Sentences,
                                ),
                            )
                        }
                    }
                }
            )
        }
    )
}