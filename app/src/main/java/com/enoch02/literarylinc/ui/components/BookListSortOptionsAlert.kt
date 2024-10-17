package com.enoch02.literarylinc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.enoch02.database.model.Sorting
import com.enoch02.literarylinc.R

@Composable
fun BookListSortOptionsAlert(
    showBookListSortOptions: Boolean,
    currentBookListSorting: Sorting,
    onDismiss: () -> Unit,
    onSortingClicked: (picked: Sorting) -> Unit
) {
    if (showBookListSortOptions) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.sorting_options_text)) },
            onDismissRequest = onDismiss,
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = {
                        Text(text = "Cancel")
                    }
                )
            },
            text = {
                val options = Sorting.entries.toTypedArray()

                Card {
                    LazyColumn(
                        content = {
                            items(options) {
                                ListItem(
                                    leadingContent = {
                                        RadioButton(
                                            selected = it == currentBookListSorting,
                                            onClick = { onSortingClicked(it) }
                                        )
                                    },
                                    headlineContent = {
                                        Text(text = it.stringify())
                                    },
                                    modifier = Modifier.clickable { onSortingClicked(it) }
                                )
                            }
                        }
                    )
                }
            }
        )
    }
}