package com.enoch02.reader.components

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.database.model.LLDocument
import java.time.Instant
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderListItem(
    document: LLDocument,
    documentInBookList: Boolean,
    cover: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAddToFavoritesClicked: () -> Unit,
    onMarkAsReadClicked: () -> Unit,
    onAddToBookList: () -> Unit,
    onRemoveFromBookList: () -> Unit
) {
    val context = LocalContext.current
    var showOptions by remember {
        mutableStateOf(false)
    }
    var showBookRemovalConfirmationDialog by remember {
        mutableStateOf(false)
    }

    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = document.name,
                    fontStyle = MaterialTheme.typography.titleMedium.fontStyle,
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
                        if (document.currentPage > 0 && document.pages > 0) {
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
                    .width(80.dp)
                    .fillMaxHeight()
            )
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val favoriteIcon =
                    if (document.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarOutline
                val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = "Add to favorites")
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onAddToFavoritesClicked,
                            content = {
                                Icon(
                                    imageVector = favoriteIcon,
                                    contentDescription = "Add to Favorites",
                                    tint = if (document.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = "Mark as Read")
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onMarkAsReadClicked,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = "Mark as read",
                                    tint = if (document.isRead || document.pages == document.currentPage) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = "Do not track this Document")
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Coming Soon!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Block,
                                    contentDescription = "Do not track document"
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = "More")
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = { showOptions = true },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More"
                                )

                                DropdownMenu(
                                    expanded = showOptions,
                                    shape = RoundedCornerShape(4.dp),
                                    onDismissRequest = { showOptions = false },
                                    content = {
                                        DropdownMenuItem(
                                            text = { Text("Add to book list") },
                                            enabled = !documentInBookList,
                                            onClick = {
                                                onAddToBookList()
                                                showOptions = false
                                            },
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Remove from book list") },
                                            enabled = documentInBookList,
                                            onClick = {
                                                showOptions = false
                                                showBookRemovalConfirmationDialog = true
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Mark as rereading") },
                                            enabled = document.isRead,
                                            onClick = {
                                                Toast.makeText(
                                                    context,
                                                    "Coming Soon!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showOptions = false
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("Share") },
                                            enabled = documentInBookList,
                                            onClick = {
                                                Toast.makeText(
                                                    context,
                                                    "Coming Soon!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showOptions = false
                                            }
                                        )

                                        HorizontalDivider()

                                        DropdownMenuItem(
                                            text = { Text("Delete", color = Color.Red) },
                                            onClick = {
                                                //TODO: also add a confirmation dialog
                                                Toast.makeText(
                                                    context,
                                                    "Coming Soon!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                showOptions = false
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            if (showBookRemovalConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showBookRemovalConfirmationDialog = false },
                    title = {
                        Text(text = "Warning")
                    },
                    text = {
                        Text(text = "Do you want to stop tracking this document in the book list? This action can not be undone.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRemoveFromBookList()
                                showBookRemovalConfirmationDialog = false
                            },
                            content = {
                                Text(text = "Yes", color = Color.Red)
                            }
                        )
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showBookRemovalConfirmationDialog = false },
                            content = { Text("No") }
                        )
                    }
                )
            }
        },
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clickable {
                onClick()
            }
    )
}

@Composable
fun ToolTipText(modifier: Modifier = Modifier, text: String) {
    Surface(
        modifier = modifier.padding(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Text color for contrast
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val now =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Date.from(Instant.now()) else Calendar.getInstance().time

    Column {
        ReaderListItem(
            LLDocument(
                name = "Hello",
                cover = "",
                contentUri = null,
                id = "1",
                lastRead = now,
                type = "PDF",
                pages = 1,
                currentPage = 1,
                isFavorite = true
            ),
            documentInBookList = true,
            cover = null,
            onClick = {},
            onAddToFavoritesClicked = {},
            onMarkAsReadClicked = {},
            onAddToBookList = {},
            onRemoveFromBookList = {}
        )

        ReaderListItem(
            LLDocument(
                name = "Hello",
                cover = "",
                contentUri = null,
                id = "1",
                author = "Enoch",
                lastRead = now,
                type = "EPUB",
            ),
            documentInBookList = false,
            cover = null,
            onClick = {},
            onAddToFavoritesClicked = {},
            onMarkAsReadClicked = {},
            onAddToBookList = {},
            onRemoveFromBookList = {}
        )
    }
}