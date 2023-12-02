package com.enoch02.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
internal fun BookDetailHeader(
    coverPath: String,
    title: String,
    author: String,
    publicationDate: String,
    genre: String,
    status: String,
    modifier: Modifier
) {
    Card {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            content = {
                AsyncImage(
                    model = coverPath,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(width = 149.dp, height = 225.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        fontWeight = MaterialTheme.typography.titleLarge.fontWeight,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (author.isNotEmpty()) {
                        Text(text = "by $author")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Genre: $genre")
                    Text(text = "Status: $status")
                }
            }
        )
    }
}
