package com.enoch02.resources.composables

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AutoScrollingText(
    text: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val textLayoutInfo = remember { mutableStateOf<TextLayoutInfo?>(null) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .horizontalScroll(scrollState)
            .onGloballyPositioned { coordinates ->
                textLayoutInfo.value = TextLayoutInfo(
                    width = with(density) { coordinates.size.width.toDp() },
                    lineCount = 1
                )
            }
            .padding(horizontal = 16.dp),
        maxLines = 1,
        softWrap = false
    )

    LaunchedEffect(textLayoutInfo.value) {
        textLayoutInfo.value?.let { layoutInfo ->
            if (layoutInfo.width > 100.dp) {
                coroutineScope.launch {
                    while (true) {
                        scrollState.animateScrollTo(
                            value = scrollState.maxValue,
                            animationSpec = tween(
                                durationMillis = scrollState.maxValue * 15,
                                easing = LinearEasing
                            )
                        )
                        delay(1500) // pause at the end
                        scrollState.animateScrollTo(
                            value = 0,
                            animationSpec = tween(
                                durationMillis = 1000,
                                easing = LinearEasing
                            )
                        )
                        delay(1500)
                    }
                }
            }
        }
    }
}

data class TextLayoutInfo(
    val width: Dp,
    val lineCount: Int
)