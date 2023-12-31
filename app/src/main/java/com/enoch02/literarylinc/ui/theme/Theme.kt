package com.enoch02.literarylinc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.setting.SettingViewModel

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/*@Composable
fun LiteraryLincTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    viewModel: SettingViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val alwaysDark by viewModel.getBooleanPreference(key = viewModel.darkModeKey)
        .collectAsState(initial = false)
    val dynamicColor by viewModel.getBooleanPreference(key = viewModel.dynamicColorKey)
        .collectAsState(initial = true)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (alwaysDark || darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        alwaysDark || darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme.switch(),
        typography = Typography,
        content = content
    )
}*/
@Composable
fun LiteraryLincTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    alwaysDark: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (alwaysDark || darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                context
            )
        }

        alwaysDark || darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme.switch(),
        typography = Typography,
        content = content
    )
}

@Composable
private fun animateColor(targetValue: Color) = animateColorAsState(
    targetValue = targetValue,
    animationSpec = tween(durationMillis = 2000),
    label = ""
).value

@Composable
fun ColorScheme.switch() = copy(
    primary = animateColor(targetValue = primary),
    onPrimary = animateColor(targetValue = onPrimary),
    primaryContainer = animateColor(targetValue = primaryContainer),
    onPrimaryContainer = animateColor(targetValue = onPrimaryContainer),
    inversePrimary = animateColor(targetValue = inversePrimary),
    secondary = animateColor(targetValue = secondary),
    onSecondary = animateColor(targetValue = onSecondary),
    secondaryContainer = animateColor(targetValue = secondaryContainer),
    onSecondaryContainer = animateColor(targetValue = onSecondaryContainer),
    tertiary = animateColor(targetValue = tertiary),
    onTertiary = animateColor(targetValue = onTertiary),
    tertiaryContainer = animateColor(targetValue = tertiaryContainer),
    onTertiaryContainer = animateColor(targetValue = onTertiaryContainer)
)


