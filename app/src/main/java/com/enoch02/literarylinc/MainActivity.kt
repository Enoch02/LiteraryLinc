package com.enoch02.literarylinc

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.enoch02.literarylinc.navigation.LiteraryLincNavHost
import com.enoch02.literarylinc.ui.theme.LiteraryLincTheme
import com.enoch02.more.settings.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SettingViewModel = hiltViewModel()
            val alwaysDark by viewModel.getBooleanPreference(key = viewModel.darkModeKey)
                .collectAsState(initial = null)
            val dynamicColor by viewModel.getBooleanPreference(key = viewModel.dynamicColorKey)
                .collectAsState(initial = null)

            if (alwaysDark != null && dynamicColor != null) {
                LiteraryLincTheme(
                    alwaysDark = alwaysDark!!,
                    dynamicColor = dynamicColor!!,
                    content = {
                        window.navigationBarColor =
                            MaterialTheme.colorScheme.surfaceContainer.toArgb()
                        val windowInsetsController =
                            WindowInsetsControllerCompat(window, window.decorView)
                        if (!isSystemInDarkTheme()) {
                            // Set to true for dark icons, false for light icons
                            windowInsetsController.isAppearanceLightNavigationBars = true
                        }


                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(rememberNestedScrollInteropConnection()),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LiteraryLincNavHost()
                        }
                    }
                )
            }

            /** [Init data before app starts](https://stackoverflow.com/questions/76301820/how-init-some-data-from-datastore-before-start-app-setcontent-jetpack-compose) **/
            val content: View = this.findViewById(android.R.id.content)
            content.viewTreeObserver.addOnPreDrawListener(
                object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        return if (alwaysDark != null && dynamicColor != null) {
                            content.viewTreeObserver.removeOnPreDrawListener(this)
                            true
                        } else {
                            false
                        }
                    }
                }
            )
        }
    }
}