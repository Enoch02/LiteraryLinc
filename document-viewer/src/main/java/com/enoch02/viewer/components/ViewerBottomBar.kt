package com.enoch02.viewer.components

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ViewerBottomBar(
    modifier: Modifier,
    visible: Boolean,
    currentPage: Int,
    pageCount: Int,
    onPageChange: (Int) -> Unit,
    visitedPages: List<Int>,
    onPageJump: (Int) -> Unit,
    onPopFromHistory: () -> Result<Int>,
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
            var previousPage by rememberSaveable { mutableIntStateOf(currentPage) }

            // Sync sliderPosition with currentPage
            LaunchedEffect(currentPage) {
                if (!isDragging) {
                    sliderPosition = currentPage.toFloat()
                }
            }

            Column {
                if (visitedPages.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onPopFromHistory().onSuccess { page ->
                                onPageChange(page)
                            }
                        },
                        content = {
                            Text("< To page ${visitedPages.last() + 1}")
                        }
                    )
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
                                        val newPage = sliderPosition.toInt()

                                        // Check for non-linear jump
                                        if (kotlin.math.abs(newPage - previousPage) > 1) {
                                            onPageJump(previousPage)
                                        }

                                        onPageChange(sliderPosition.toInt())
                                        previousPage = newPage
                                    },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        )
                    }
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    ViewerBottomBar(
        modifier = Modifier,
        visible = true,
        currentPage = 50,
        pageCount = 100,
        onPageChange = {

        },
        visitedPages = listOf(0),
        onPageJump = {

        },
        onPopFromHistory = {
            Result.success(1)
        }
    )
}