package com.enoch02.bookdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun BasicInfoView(
    modifier: Modifier = Modifier,
    title: String,
    author: String,
    pagesRead: Int,
    pageCount: Int
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        if (author.isNotBlank()) {
            Text(text = " by $author", style = MaterialTheme.typography.labelSmall)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$pagesRead/$pageCount",
            style = MaterialTheme.typography.titleSmall
        )
    }
}