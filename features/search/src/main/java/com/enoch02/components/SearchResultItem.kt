package com.enoch02.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.search.R

@Composable
fun SearchResultItem(
    title: String,
    author: List<String>,
    coverUrl: String,
    onAddClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (author.isNotEmpty()) {
                Text(
                    text = "by ${author.first()}",
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        },
        leadingContent = {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(width = 50.dp, height = 80.dp)
            )
        },
        trailingContent = {
            OutlinedIconButton(
                onClick = onAddClick,
                shape = RectangleShape,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_add_24),
                        contentDescription = null
                    )
                }
            )
        }
    )
}