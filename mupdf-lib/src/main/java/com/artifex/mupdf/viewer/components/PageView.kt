package com.artifex.mupdf.viewer.components

import android.graphics.Bitmap
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalDragOrCancellation
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
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
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit
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
                                        val leftEdge = width * 0.2f // 20% from the left
                                        val rightEdge = width * 0.8f // 20% from the right

                                        when {
                                            down.position.x < leftEdge -> onNavigateBack() // Left edge tap
                                            down.position.x > rightEdge -> onNavigateForward() // Right edge tap
                                            down.position.x > width / 4 &&
                                                    down.position.x < width * 3 / 4 &&
                                                    down.position.y > height / 4 &&
                                                    down.position.y < height * 3 / 4 -> {
                                                onCenterTap() // Center area tap
                                            }
                                        }
                                    } else {
                                        // Handle horizontal drag/swipe
                                        val dragAmount = awaitHorizontalDragOrCancellation(drag.id)
                                        if (dragAmount != null) {
                                            if (dragAmount.positionChange().x > 0) {
                                                onNavigateBack()
                                            } else {
                                                onNavigateForward()
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
    bitmaps: List<Bitmap?>
) {
    if (bitmaps.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), content = { CircularProgressIndicator() })
    } else {
        LazyRow(
            modifier = modifier.fillMaxSize(),
            content = {
                itemsIndexed(
                    items = bitmaps,
                    key = { index, _ -> index },
                    itemContent = { _, item ->
                        AsyncImage(
                            model = item,
                            contentDescription = null,
                            modifier.fillParentMaxSize()
                        )
                    }
                )
            }
        )
    }
}