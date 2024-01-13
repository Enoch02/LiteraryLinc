package com.enoch02.more.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.enoch02.more.R

@Composable
fun ErrorAlertDialog(message: String, onDismiss: () -> Unit) {
    val clipboardManager =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val clip = ClipData.newPlainText("error message", message)
                    clipboardManager.setPrimaryClip(clip)
                },
                content = {
                    Text(text = "Copy")
                }
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                content = {
                    Text(text = "Close")
                }
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.round_error_24),
                contentDescription = null
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                content = {
                    Text(text = message, modifier = Modifier.fillMaxSize())
                }
            )
        }
    )
}