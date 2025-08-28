package com.enoch02.reader.components

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.enoch02.database.model.LLDocument
import com.enoch02.resources.LLString
import com.enoch02.resources.extensions.adaptiveWidth
import java.time.Instant
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderListItem(
    document: LLDocument,
    documentInBookList: Boolean,
    cover: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onAddToFavoritesClicked: () -> Unit,
    onMarkAsReadClicked: () -> Unit,
    onAddToBookList: () -> Unit,
    onRemoveFromBookList: () -> Unit,
    onToggleAutoTracking: () -> Unit,
    onDeleteDocument: () -> Unit,
    onShare: () -> Unit,
) {
    val context = LocalContext.current
    var showOptions by remember {
        mutableStateOf(false)
    }
    var showConfirmationDialog by remember {
        mutableStateOf(false)
    }
    var confirmationDialogText by remember {
        mutableIntStateOf(LLString.blank)
    }

    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = document.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (document.author.isNotEmpty()) {
                    Text(
                        text = document.author,
                        maxLines = 1,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    )
                }

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
                    }
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
                modifier = Modifier.adaptiveWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val favoriteIcon =
                    if (document.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarOutline
                val tooltipPosition = TooltipDefaults.rememberPlainTooltipPositionProvider()

                TooltipBox(
                    tooltip = {
                        ToolTipText(text = stringResource(LLString.addToFavorites))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onAddToFavoritesClicked,
                            content = {
                                Icon(
                                    imageVector = favoriteIcon,
                                    contentDescription = stringResource(LLString.addToFavorites),
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
                        ToolTipText(text = stringResource(LLString.markAsRead))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onMarkAsReadClicked,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.DoneAll,
                                    contentDescription = stringResource(LLString.markAsRead),
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
                        ToolTipText(text = stringResource(LLString.doNotAutoTrack))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = onToggleAutoTracking,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Block,
                                    contentDescription = stringResource(LLString.doNotAutoTrack),
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
                        ToolTipText(text = stringResource(LLString.more))
                    },
                    state = rememberTooltipState(isPersistent = false),
                    positionProvider = tooltipPosition,
                    content = {
                        IconButton(
                            onClick = { showOptions = true },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = stringResource(LLString.more)
                                )

                                DropdownMenu(
                                    expanded = showOptions,
                                    shape = RoundedCornerShape(4.dp),
                                    onDismissRequest = { showOptions = false },
                                    content = {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(LLString.addToBooklist)) },
                                            enabled = !documentInBookList,
                                            onClick = {
                                                onAddToBookList()
                                                showOptions = false
                                            },
                                        )

                                        if (document.isRead) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(LLString.markAsRereading)) },
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
                                            text = { Text(stringResource(LLString.share)) },
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
                                                        text = stringResource(LLString.removeFromBooklist),
                                                        color = Color.Red
                                                    )
                                                },
                                                onClick = {
                                                    showOptions = false
                                                    confirmationDialogText =
                                                        LLString.documentRemovalWarning
                                                    showConfirmationDialog = true
                                                }
                                            )
                                        }

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(LLString.delete),
                                                    color = Color.Red
                                                )
                                            },
                                            onClick = {
                                                showOptions = false
                                                confirmationDialogText =
                                                    LLString.documentDeletionWarning
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
        },
        modifier = modifier
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    )

    ConfirmationDialog(
        showConfirmationDialog = showConfirmationDialog,
        onDismiss = {
            showConfirmationDialog = false
        },
        onConfirm = {
            when (confirmationDialogText) {
                LLString.documentRemovalWarning -> {
                    onRemoveFromBookList()
                }

                LLString.documentDeletionWarning -> {
                    onDeleteDocument()
                }
            }

            showConfirmationDialog = false
        },
        title = stringResource(LLString.warning),
        text = stringResource(confirmationDialogText)
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
            onLongClick = {},
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
            onLongClick = {},
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