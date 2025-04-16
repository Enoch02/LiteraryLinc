package com.enoch02.bookdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.enoch02.database.model.Book

@Composable
fun QuickActionView(
    modifier: Modifier = Modifier,
    currentStatus: String,
    currentRating: Int,
    onBookStatusChange: (newStatus: Book.Companion.BookStatus) -> Unit,
    onBookRatingChange: (newRating: Int) -> Unit
) {
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showRatingDropdown by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        //TODO: implement this to change the book status after a
        // dropdown showing available choices are displayed. Also
        // extract string to LLString
        Button(
            onClick = {
                showStatusDropdown = true
            },
            content = {
                Text(currentStatus.uppercase())

                DropdownMenu(
                    expanded = showStatusDropdown,
                    onDismissRequest = { showStatusDropdown = false },
                    content = {
                        Book.Companion.BookStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.strName) },
                                onClick = {
                                    onBookStatusChange(status)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { showRatingDropdown = true },
            content = {
                Text("RATING: ${if (currentRating > 0) currentRating else "-"}")

                DropdownMenu(
                    showRatingDropdown,
                    onDismissRequest = { showRatingDropdown = false },
                    content = {
                        (1..10).forEach { rating ->
                            DropdownMenuItem(
                                text = { Text("$rating") },
                                onClick = {
                                    onBookRatingChange(rating)
                                    showRatingDropdown = false
                                }
                            )
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}