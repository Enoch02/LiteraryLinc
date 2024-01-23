package com.enoch02.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.enoch02.search.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchHistoryItem(text: String, onClick: () -> Unit, onLongPress: () -> Unit) {
    var showRemoveItemDialog by rememberSaveable { mutableStateOf(false) }

    ListItem(
        leadingContent = {
            Icon(
                painter = painterResource(id = R.drawable.round_history_24),
                contentDescription = null
            )
        },
        headlineContent = { Text(text = text) },
        modifier = Modifier.combinedClickable(
            onClick = { onClick() },
            onLongClick = {
                showRemoveItemDialog = true
            }
        )
    )

    //TODO: extract string resource
    if (showRemoveItemDialog) {
        AlertDialog(
            title = { Text(text = text) },
            text = { Text(text = "Remove from search history?") },
            onDismissRequest = { showRemoveItemDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onLongPress()
                        showRemoveItemDialog = false
                    },
                    content = {
                        Text(text = "Remove")
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveItemDialog = false },
                    content = {
                        Text(text = "Cancel")
                    }
                )
            }
        )
    }
}