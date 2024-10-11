package com.enoch02.reader.components

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.database.model.LLDocument
import java.time.Instant
import java.util.Calendar
import java.util.Date

@Composable
fun DocumentListItem(
    document: LLDocument,
    cover: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var showOptions by remember {
        mutableStateOf(false)
    }

    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = document.name,
                    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                    fontWeight = MaterialTheme.typography.titleMedium.fontWeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = document.author,
                    maxLines = 1,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    modifier = Modifier.alpha(if (document.author.isNotEmpty()) 1f else 0f)
                )

                Text(
                    text = "${document.type}, ${document.sizeInMb}MB",
                    maxLines = 1,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                )


                LinearProgressIndicator(
                    progress = {
                        if (document.currentPage > 0 && document.pages >= document.currentPage) {
                            document.currentPage.toFloat() / document.pages.toFloat()
                        } else {
                            0f
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        leadingContent = {
            AsyncImage(
                model = cover,
                contentDescription = null,
                modifier = Modifier
                    .size(width = 50.dp, height = 80.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { /*TODO*/ },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.StarOutline,
                            contentDescription = "Add to favorites"
                        )
                    }
                )

                IconButton(
                    onClick = { /*TODO*/ },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.DoneAll,
                            contentDescription = "Mark as completed"
                        )
                    }
                )

                IconButton(
                    onClick = { /*TODO*/ },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share Document"
                        )
                    }
                )


                IconButton(
                    onClick = { /*TODO*/ },
                    content = {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More"
                        )
                    }
                )
            }
        },
        modifier = modifier.clickable {
            onClick()
        }
    )
}

@Preview
@Composable
private fun Preview() {
    val now =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Date.from(Instant.now()) else Calendar.getInstance().time

    Column {
        DocumentListItem(
            LLDocument(
                name = "Hello",
                cover = "",
                contentUri = null,
                id = "1",
                lastRead = now,
                type = "PDF"
            ),
            cover = null,
            onClick = {}
        )

        DocumentListItem(
            LLDocument(
                name = "Hello",
                cover = "",
                contentUri = null,
                id = "1",
                author = "Enoch",
                lastRead = now,
                type = "EPUB"
            ),
            cover = null,
            onClick = {}
        )
    }
}