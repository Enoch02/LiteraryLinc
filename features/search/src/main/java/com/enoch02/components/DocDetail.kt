package com.enoch02.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enoch02.database.model.Book
import com.enoch02.search_api.Doc

//TODO: extract string resources
//TODO: add option to select multiple authors
@Composable
fun DocDetail(doc: Doc, coverUrl: String, onDismiss: () -> Unit, onConfirm: (book: Book) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedAuthor by remember { mutableStateOf("") }
    var selectedPublisher by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("") }
    var selectedPublishYear by remember { mutableStateOf("") }
    var selectedIsbn by remember { mutableStateOf("") }

    LaunchedEffect(
        key1 = Unit,
        block = {
            title = doc.title.toString()
            selectedAuthor = doc.author?.first() ?: ""
            selectedPublisher = doc.publisher?.first() ?: ""
            selectedLanguage = doc.language?.first() ?: ""
            selectedPublishYear = doc.publishYear?.first() ?: ""
            selectedIsbn = doc.isbn?.first() ?: ""
        }
    )


    /*TODO: Something here makes the app unresponsive (might be related to the dropdowns)*/
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    /*TODO: I can add modified info to the dab?
                    * I can't modify the doc objects and i don't want to
                    * attach this composable to the viewmodel...
                    * */
                    /*onConfirm()*/
                },
                content = {
                    Text(text = "Add")
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text(text = "Cancel")
                }
            )
        },
        title = {
            Text(text = "Modify Book Details")
        },
        text = {
            Column {
                LazyColumn(
                    verticalArrangement = Arrangement.SpaceEvenly,
                    content = {

                        item {
                            OutlinedTextField(
                                value = doc.title.toString(),
                                onValueChange = {},
                                label = { Text(text = "Title") }
                            )
                        }

                        if (!doc.author.isNullOrEmpty()) {
                            item {
                                FormSpinner(
                                    label = "Author(s)",
                                    options = doc.author,
                                    selectedOption = selectedAuthor,
                                    onSelectionChange = { selectedAuthor = it }
                                )
                            }
                        }

                        if (!doc.publisher.isNullOrEmpty()) {
                            item {
                                FormSpinner(
                                    label = "Publisher(s)",
                                    options = doc.publisher,
                                    selectedOption = selectedPublisher,
                                    onSelectionChange = { selectedPublisher = it }
                                )
                            }
                        }

                        if (!doc.language.isNullOrEmpty()) {
                            item {
                                FormSpinner(
                                    label = "Language(s)",
                                    options = doc.language,
                                    selectedOption = selectedLanguage,
                                    onSelectionChange = { selectedLanguage = it }
                                )
                            }
                        }

                        if (!doc.publishYear.isNullOrEmpty()) {
                            item {
                                FormSpinner(
                                    label = "Publisher(s)",
                                    options = doc.publishYear,
                                    selectedOption = selectedPublishYear,
                                    onSelectionChange = { selectedPublishYear = it }
                                )
                            }
                        }

                        if (!doc.isbn.isNullOrEmpty()) {
                            item {
                                FormSpinner(
                                    label = "ISBN(s)",
                                    options = doc.isbn,
                                    selectedOption = selectedIsbn,
                                    onSelectionChange = { selectedIsbn = it }
                                )
                            }
                        }
                    },
                    contentPadding = PaddingValues(vertical = 12.dp)
                )
            }
        }
    )
}


@Composable
internal fun FormSpinner(
    label: String,
    options: List<String>,
    selectedOption: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        var expanded by remember { mutableStateOf(false) }

        OutlinedTextField(
            readOnly = true,
            value = selectedOption,
            label = { Text(text = label) },
            onValueChange = { },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                    content = {
                        Icon(
                            imageVector = if (!expanded) {
                                Icons.Rounded.KeyboardArrowDown
                            } else {
                                Icons.Rounded.KeyboardArrowUp
                            }, contentDescription = null
                        )
                    }
                )
            },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            content = {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = {
                            Text(text = selectionOption)
                        },
                        onClick = {
                            onSelectionChange(selectionOption)
                            expanded = false
                        }
                    )
                }
            }
        )
    }
}
