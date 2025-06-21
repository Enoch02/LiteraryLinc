package com.enoch02.resources.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FlipToBack
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.enoch02.resources.LLString

@Composable
fun ListSelectionTopRow(
    modifier: Modifier = Modifier,
    visible: Boolean,
    selectionCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    onDelete: () -> Unit,
    additionalIcons: @Composable() (RowScope.() -> Unit) = {}
) {
    var showDeletionConfirmation by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = visible,
        content = {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(42.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        IconButton(
                            onClick = onClearSelection,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(LLString.clearSelection)
                                )
                            }
                        )

                        Text(
                            text = "$selectionCount",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onSelectAll,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.SelectAll,
                                    contentDescription = stringResource(LLString.selectAll)
                                )
                            }
                        )

                        IconButton(
                            onClick = onInvertSelection,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.FlipToBack,
                                    contentDescription = stringResource(LLString.invertSelection)
                                )
                            }
                        )


                        IconButton(
                            onClick = { showDeletionConfirmation = true },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = stringResource(LLString.deleteSelection)
                                )
                            }
                        )

                        additionalIcons()
                    }
                }
            )
        }
    )

    ConfirmDeletionDialog(
        visible = showDeletionConfirmation,
        onDismiss = { showDeletionConfirmation = false },
        onConfirm = {
            onDelete()
            showDeletionConfirmation = false
        },
        message = stringResource(
            LLString.multiDeletionWarning,
            selectionCount
        )
    )
}

@Composable
fun ConfirmDeletionDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (visible) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = { Text(text = stringResource(LLString.warning)) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    content = { Text(text = stringResource(LLString.yes), color = Color.Red) }
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    content = { Text(text = stringResource(LLString.no)) }
                )
            }
        )
    }
}