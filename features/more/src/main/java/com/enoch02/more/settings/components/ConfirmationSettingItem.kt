package com.enoch02.more.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLString

@Composable
fun ConfirmationSettingItem(
    label: String,
    description: String = "",
    onClick: () -> Unit,
    alertTitle: String = "Warning",
    alertMsg: String
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(text = label)
        },
        supportingContent = {
            if (description.isNotEmpty()) {
                Text(text = description)
            }
        },
        modifier = Modifier.clickable {
            showConfirmationDialog = true
        },
        tonalElevation = 10.dp
    )

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(alertTitle) },
            text = { Text(alertMsg) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClick()
                        showConfirmationDialog = false
                    },
                    content = {
                        Text(stringResource(LLString.yes), color = Color.Red)
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmationDialog = false
                    },
                    content = {
                        Text(stringResource(LLString.yes))
                    }
                )
            }
        )
    }
}