package com.enoch02.viewer.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.enoch02.viewer.DocumentInfo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun DocumentInfoDialog(info: DocumentInfo, visible: Boolean, onDismiss: () -> Unit) {
    val docInfo = with(info) {
        listOf(
            title,
            author,
            subject,
            creationDate,
            modDate,
            creator,
            producer,
            keywords,
            encryption
        )
    }

    if (visible) {
        AlertDialog(
            title = { Text("Document Information") },
            text = {
                Card {
                    LazyColumn {
                        var visibleItemCount = 0

                        items(docInfo.size) { index ->
                            if (docInfo[index].isNotBlank()) {
                                visibleItemCount++

                                ListItem(
                                    headlineContent = {
                                        Text(resolveLabelFor(index))
                                    },
                                    supportingContent = {
                                        val infoItem = docInfo[index]

                                        if (infoItem.startsWith("D")) {
                                            Text(parsePdfDate(infoItem))
                                        } else {
                                            Text(infoItem)
                                        }
                                    }
                                )

                                // Show HorizontalDivider if this is not the last visible item
                                if (visibleItemCount < docInfo.count { it.isNotBlank() }) {
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { /* Optional confirm button */ },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            },
            onDismissRequest = onDismiss
        )
    }
}

private fun resolveLabelFor(index: Int): String {
    return when (index) {
        0 -> "Title"
        1 -> "Author"
        2 -> "Subject"
        3 -> "Creation Date"
        4 -> "Modification Date"
        5 -> "PDF Creator"
        6 -> "PDF Producer"
        7 -> "Keywords"
        8 -> "Encryption"
        else -> ""
    }
}

private fun parsePdfDate(pdfDate: String): String {
    try {
        val cleanDate = pdfDate.replace("D:", "")
            .replace("'", "")
            .substring(0, 14)

        val parseFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH)
        parseFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = parseFormat.parse(cleanDate)

        val outputFormat = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.ENGLISH)
        outputFormat.timeZone = TimeZone.getDefault()

        return date?.let { outputFormat.format(it) } ?: "Invalid Date"
    } catch (e: Exception) {
        return "Invalid Date"
    }
}
