package com.enoch02.more.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp

@Composable
fun RenderMethodSelector(
    modifier: Modifier = Modifier,
    selectedMethod: Int,
    onScaleChange: (Int) -> Unit
) {
    var showOptions by remember {
        mutableStateOf(false)
    }
    val names = listOf("Android draw device", "Bitmap scaling", "Pixmap conversion")

    ListItem(
        headlineContent = {
            Text("Document render scale")
        },
        supportingContent = {
            Text("Selected method: ${names[selectedMethod]}")
        },
        modifier = modifier.clickable {
            showOptions = true
        },
        tonalElevation = 10.dp
    )

    if (showOptions) {
        AlertDialog(
            onDismissRequest = {
                showOptions = false
            },
            title = {
                Text("Select Scale")
            },
            text = {
                Card {
                    LazyColumn {
                        items(3, key = { it }) { index ->
                            ListItem(
                                headlineContent = {
                                    Text(names[index])
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = index == selectedMethod,
                                        onClick = {
                                            onScaleChange(index)
                                            showOptions = false
                                        }
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onScaleChange(index)
                                    showOptions = false
                                }
                            )
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showOptions = false }) {
                    Text("Cancel")
                }
            },
            confirmButton = {

            }
        )
    }
}