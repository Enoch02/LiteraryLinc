package com.artifex.mupdf.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.artifex.mupdf.fitz.Document

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    visible: Boolean,
    modifier: Modifier = Modifier,
    documentTitle: String,
    onLink: () -> Unit,
    onSearch: () -> Unit,
    onOutline: () -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        content = {
            TopAppBar(
                title = {
                    Text(
                        text = documentTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        content = {
                            Icon(imageVector = Icons.Rounded.Link, contentDescription = null)
                        },
                        onClick = onLink
                    )

                    IconButton(
                        content = {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        },
                        onClick = onSearch
                    )

                    IconButton(
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.List,
                                contentDescription = null
                            )
                        },
                        onClick = onOutline
                    )
                }
            )
        }
    )
}

@Preview
@Composable
private fun Preview() {
    ReaderTopBar(
        visible = true,
        documentTitle = "Hello, World",
        onLink = {},
        onSearch = {},
        onOutline = {}
    )
}