package com.artifex.mupdf.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ReaderBottomBar(
    modifier: Modifier,
    visible: Boolean,
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit
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
            var sliderPosition by remember { mutableFloatStateOf(currentPage.toFloat()) }
            var isDragging by remember { mutableStateOf(false) }

            // Sync sliderPosition with currentPage
            LaunchedEffect(currentPage) {
                if (!isDragging) {
                    sliderPosition = currentPage.toFloat()
                }
            }

            BottomAppBar(
                content = {
                    Column(
                        content = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                content = {
                                    Text(if (isDragging) "${sliderPosition.toInt() + 1}" else "${currentPage + 1}")
                                    Text("/$pageCount")
                                }
                            )

                            Slider(
                                value = sliderPosition,
                                valueRange = 0f..(pageCount - 1).toFloat(),
                                steps = pageCount - 1,
                                onValueChange = {
                                    isDragging = true
                                    sliderPosition = it
                                },
                                onValueChangeFinished = {
                                    isDragging = false
                                    onPageChange(sliderPosition.toInt())
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