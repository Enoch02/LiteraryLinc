package com.enoch02.literarylinc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.enoch02.database.model.ReaderSorting
import com.enoch02.resources.LLString

@Composable
fun ReaderListSortOptionsAlert(
    showReaderListSortOptions: Boolean,
    currentReaderListSorting: ReaderSorting,
    onDismiss: () -> Unit,
    onSortingClicked: (picked: ReaderSorting) -> Unit
) {
    if (showReaderListSortOptions) {
        AlertDialog(
            title = { Text(text = stringResource(LLString.sortingOptions)) },
            onDismissRequest = onDismiss,
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = {
                        Text(text = stringResource(LLString.cancel))
                    }
                )
            },
            text = {
                val readerSortOptions =
                    ReaderSorting.entries.toTypedArray()

                Card {
                    readerSortOptions.forEach {
                        ListItem(
                            leadingContent = {
                                RadioButton(
                                    selected = it == currentReaderListSorting,
                                    onClick = { onSortingClicked(it) }
                                )
                            },
                            headlineContent = {
                                Text(text = it.value)
                            },
                            modifier = Modifier.clickable { onSortingClicked(it) }
                        )
                    }
                }
            }
        )
    }
}