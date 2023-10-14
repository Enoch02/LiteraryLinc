package com.enoch02.addbook

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.enoch02.database.model.BookType
import com.enoch02.composables.BackArrowButton
import com.enoch02.composables.FormSpinner
import com.enoch02.composables.FormTextField
import com.enoch02.composables.ImagePicker
import com.enoch02.database.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(
    navController: NavController,
    modifier: Modifier,
    viewModel: AddBookViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var bookTitle by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var coverImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pageNumber by rememberSaveable { mutableStateOf("0") }
    val options = BookType.values().map {
        it.name.lowercase().replaceFirstChar { fc -> fc.uppercaseChar() }
            .replace("_", " or ")
    }
    var selectedType by rememberSaveable { mutableStateOf(options[1]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_a_new_book_text)) },
                navigationIcon = {
                    BackArrowButton {
                        navController.popBackStack()
                    }
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
                                    coverImageUri = coverImageUri
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                //TODO: replace with something more appropriate
                                Column {
                                    Text(text = "Add Page Number", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = pageNumber,
                                        onValueChange = { pageNumber = it },
                                        singleLine = true,
                                        modifier = Modifier.width(80.dp)
                                    )
                                }
                            }
                        )
                    }
                    item {
                        //TODO: Change focus to the next text field when enter is pressed.
                        FormTextField(
                            label = "Book Title",
                            value = bookTitle,
                            onValueChange = { bookTitle = it }
                        )
                    }

                    item {
                        FormTextField(
                            label = "Author Name",
                            value = author,
                            onValueChange = { author = it })
                    }

                    item {
                        //TODO: selectedType should be a BookType object
                        FormSpinner(
                            label = "Category",
                            options = options,
                            selectedOption = selectedType,
                            onSelectionChange = { selectedType = it })
                    }

                    item {
                        Button(
                            onClick = {
                                try {
                                    val newBook = Book.createBook(
                                        title = bookTitle,
                                        /*type = selectedType*/
                                        type = BookType.BOOK
                                    )
                                    viewModel.addNewBook(newBook)
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "${e.message}",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            },
                            content = {
                                Text(text = "Save Book Info")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun Preview() {
    AddBookScreen(
        navController = rememberNavController(), modifier = Modifier.padding(8.dp)
    )
}
