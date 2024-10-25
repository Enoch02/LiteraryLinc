package com.artifex.mupdf.viewer.components

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalDragOrCancellation
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import coil.compose.AsyncImage
import kotlinx.coroutines.coroutineScope

@Composable
fun PageView(
    modifier: Modifier = Modifier,
    page: Bitmap?,
    onCenterTap: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    Column(
        modifier = modifier,
        content = {
            AsyncImage(
                model = page,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        coroutineScope {
                            while (true) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()
                                    val width = size.width
                                    val height = size.height

                                    // Handle Tap if no dragging is detected
                                    val drag = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                                        change.consume()
                                    }
                                    if (drag == null) {
                                        // It's a tap
                                        if (down.position.x > width / 4 &&
                                            down.position.x < width * 3 / 4 &&
                                            down.position.y > height / 4 &&
                                            down.position.y < height * 3 / 4
                                        ) {
                                            onCenterTap()
                                        }
                                    } else {
                                        // Handle horizontal drag/swipe
                                        val dragAmount = awaitHorizontalDragOrCancellation(drag.id)
                                        if (dragAmount != null) {
                                            if (dragAmount.positionChange().x > 0) {
                                                onSwipeRight()
                                            } else {
                                                onSwipeLeft()
                                            }
                                            dragAmount.consume()
                                        }
                                    }
                                }
                            }
                        }
                    }
            )
        }
    )
}


@Composable
fun NewPageView(
    modifier: Modifier = Modifier,
    pageCount: Int,
    currentPage: Bitmap?
) {
    LazyRow(
        modifier = modifier.fillMaxSize(),
        content = {
            items(
                count = pageCount,
                key = { it },
                itemContent = {
                    AsyncImage(
                        model = currentPage,
                        contentDescription = null,
                    )
                }
            )
        }
    )
}