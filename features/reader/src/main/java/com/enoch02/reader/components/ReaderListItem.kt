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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.database.model.LLDocument
import com.enoch02.reader.R
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
    onRemoveFromBookList: () -> Unit,
    onToggleAutoTracking: () -> Unit,
    onDeleteDocument: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var showOptions by remember {
        mutableStateOf(false)
    }
    var showConfirmationDialog by remember {
        mutableStateOf(false)
    }
    var confirmationDialogText by remember {
        mutableIntStateOf(R.string.blank)
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
                        ToolTipText(text = stringResource(R.string.add_to_favorites))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onAddToFavoritesClicked,
                            content = {
                                Icon(
                                    imageVector = favoriteIcon,
                                    contentDescription = stringResource(R.string.add_to_favorites),
                                    tint = if (document.isFavorite) {
                                        ReaderColors.activatedIconColor
                                    } else {
                                        LocalContentColor.current
                                    }
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = stringResource(R.string.mark_as_read))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onMarkAsReadClicked,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = stringResource(R.string.mark_as_read),
                                    tint = if (document.isRead || document.pages == document.currentPage) {
                                        ReaderColors.activatedIconColor
                                    } else {
                                        LocalContentColor.current
                                    }
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = stringResource(R.string.do_not_auto_track))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onToggleAutoTracking,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Block,
                                    contentDescription = stringResource(R.string.do_not_auto_track),
                                    tint = if (document.autoTrackable) {
                                        LocalContentColor.current
                                    } else {
                                        ReaderColors.blockedIconColor
                                    }
                                )
                            }
                        )
                    }
                )

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = stringResource(R.string.more))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = { showOptions = true },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = stringResource(R.string.more)
                                )

                                DropdownMenu(
                                    expanded = showOptions,
                                    shape = RoundedCornerShape(4.dp),
                                    onDismissRequest = { showOptions = false },
                                    content = {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.add_to_book_list)) },
                                            enabled = !documentInBookList,
                                            onClick = {
                                                onAddToBookList()
                                                showOptions = false
                                            },
                                        )

                                        if (document.isRead) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.mark_as_rereading)) },
                                                onClick = {
                                                    Toast.makeText(
                                                        context,
                                                        "Coming Soon!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    showOptions = false
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.share)) },
                                            onClick = {
                                                onShare()
                                                showOptions = false
                                            }
                                        )

                                        HorizontalDivider()

                                        if (documentInBookList) {
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = stringResource(R.string.remove_from_book_list),
                                                        color = Color.Red
                                                    )
                                                },
                                                onClick = {
                                                    showOptions = false
                                                    confirmationDialogText =
                                                        R.string.document_removal_warning
                                                    showConfirmationDialog = true
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(R.string.delete),
                                                    color = Color.Red
                                                )
                                            },
                                            onClick = {
                                                showOptions = false
                                                confirmationDialogText =
                                                    R.string.document_deletion_warning
                                                showConfirmationDialog = true
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            ConfirmationDialog(
                showConfirmationDialog = showConfirmationDialog,
                onDismiss = {
                    showConfirmationDialog = false
                },
                onConfirm = {
                    when (confirmationDialogText) {
                        R.string.document_removal_warning -> {
                            onRemoveFromBookList()
                        }

                        R.string.document_deletion_warning -> {
                            onDeleteDocument()
                        }
                    }

                    showConfirmationDialog = false
                },
                title = stringResource(R.string.warning),
                text = stringResource(confirmationDialogText)
            )
        },
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clickable {
                onClick()
            }
    )
}

@Composable
private fun ToolTipText(modifier: Modifier = Modifier, text: String) {
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

@Composable
private fun ConfirmationDialog(
    modifier: Modifier = Modifier,
    showConfirmationDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String,
    text: String
) {
    if (showConfirmationDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Text(text = text)
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    content = {
                        Text(text = "Yes", color = Color.Red)
                    }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = { Text("No") }
                )
            }
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
            onRemoveFromBookList = {},
            onToggleAutoTracking = {},
            onShare = {},
            onDeleteDocument = {}
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
            onRemoveFromBookList = {},
            onToggleAutoTracking = {},
            onShare = {},
            onDeleteDocument = {}
        )
    }
}