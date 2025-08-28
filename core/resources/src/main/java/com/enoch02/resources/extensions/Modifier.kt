package com.enoch02.resources.extensions

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.adaptiveWidth(
    maxTabletWidth: Dp = 600.dp
): Modifier {
    val configuration = LocalConfiguration.current
    //TODO: fix the squiggle
    val isTablet = configuration.screenWidthDp >= 600
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    return if (isTablet || isLandscape) {
        this.widthIn(max = maxTabletWidth)
    } else {
        this.fillMaxWidth()
    }
}