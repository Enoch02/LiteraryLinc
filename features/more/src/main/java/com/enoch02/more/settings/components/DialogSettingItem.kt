package com.enoch02.more.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLString

@Composable
fun DialogSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    values: List<String>,
    selected: String,
    tonalElevation: Dp,
    onSelectionChange: (value: String) -> Unit
) {
    var showOptionDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(text = stringResource(LLString.fileScanFrequency)) },
        supportingContent = { Text(text = selected) },
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                showOptionDialog = true
            },
        tonalElevation = tonalElevation
    )

    if (showOptionDialog) {
        AlertDialog(
            onDismissRequest = { showOptionDialog = false },
            title = { Text(text = title) },
            text = {
                Card {
                    LazyColumn {
                        items(values) { value ->
                            ListItem(
                                leadingContent = {
                                    RadioButton(
                                        selected = value == selected,
                                        onClick = {
                                            onSelectionChange(value)
                                            showOptionDialog = false
                                        }
                                    )
                                },
                                headlineContent = { Text(text = value) },
                                modifier = Modifier.clickable {
                                    onSelectionChange(value)
                                    showOptionDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showOptionDialog = false },
                    content = { Text(stringResource(LLString.cancel)) }
                )
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    DialogSettingItem(
        title = "Title",
        values = listOf("A", "B", "C"),
        selected = "B",
        tonalElevation = 10.dp
    ) { }
}