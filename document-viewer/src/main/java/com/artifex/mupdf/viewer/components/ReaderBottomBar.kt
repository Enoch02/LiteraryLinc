package com.artifex.mupdf.viewer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ReaderBottomBar(
    modifier: Modifier,
    visible: Boolean,
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Float) -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        content = {
            BottomAppBar(
                content = {
                    Column(
                        content = {
                            Text(
                                text = "$currentPage/$pageCount",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Slider(
                                value = currentPage.toFloat(),
                                valueRange = 1f..pageCount.toFloat(),
                                steps = pageCount,
                                onValueChange = {
                                    onPageChange(it)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    )
                }
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ReaderBottomBar(
        modifier = Modifier,
        visible = true,
        currentPage = 50,
        pageCount = 100,
        onPageChange = {

        }
    )
}