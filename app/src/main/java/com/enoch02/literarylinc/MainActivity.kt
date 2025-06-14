package com.enoch02.literarylinc

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.literarylinc.navigation.LiteraryLincNavHost
import com.enoch02.resources.theme.LiteraryLincTheme
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: InitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { viewModel.isLoading }
        }
        super.onCreate(savedInstanceState)

        setContent {
            val alwaysDark by viewModel.alwaysDark.collectAsState(initial = false)
            val dynamicColor by viewModel.dynamicColor.collectAsState(initial = false)

            enableEdgeToEdge()

            LiteraryLincTheme(
                alwaysDark = alwaysDark,
                dynamicColor = dynamicColor,
                content = {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(rememberNestedScrollInteropConnection()),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (!viewModel.isLoading) {
                            LiteraryLincNavHost()
                        } else {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                LLSplashScreen()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LLSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(com.enoch02.resources.R.drawable.app_icon_svg),
            contentDescription = "Splash Icon",
            modifier = Modifier.size(80.dp)
        )
    }
}


@HiltViewModel
class InitViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
    ViewModel() {
    var isLoading by mutableStateOf(true)
    val alwaysDark = getBooleanPreference(key = SettingsRepository.BooleanPreferenceType.DARK_MODE)
    val dynamicColor =
        getBooleanPreference(key = SettingsRepository.BooleanPreferenceType.DYNAMIC_COLOR)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            delayForPreferenceLoad()
        }
    }

    private fun getBooleanPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }

    private suspend fun delayForPreferenceLoad() {
        delay(500)
        isLoading = false
    }
}