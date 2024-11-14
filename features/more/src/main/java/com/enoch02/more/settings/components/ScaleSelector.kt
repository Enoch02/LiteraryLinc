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
fun ScaleSelector(
    modifier: Modifier = Modifier,
    selectedScale: Float,
    onScaleChange: (Float) -> Unit
) {
    var showOptions by remember {
        mutableStateOf(false)
    }

    ListItem(
        headlineContent = {
            Text("Document render scale")
        },
        supportingContent = {
            val currentScale = if (selectedScale == 0f) {
                "Device Default"
            } else {
                "${selectedScale.toInt()}x"
            }

            Text("Current scale: $currentScale")
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
                        items(6, key = { it }) { index ->
                            ListItem(
                                headlineContent = {
                                    val text = if (index == 0) "Device Default" else "${index}x"
                                    Text(text)
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = index.toFloat() == selectedScale,
                                        onClick = {
                                            onScaleChange(index.toFloat())
                                            showOptions = false
                                        }
                                    )
                                },
                                modifier = Modifier.clickable {
                                    onScaleChange(index.toFloat())
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