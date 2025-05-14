package com.enoch02.literarylinc

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.enoch02.literarylinc.navigation.LiteraryLincNavHost
import com.enoch02.resources.theme.LiteraryLincTheme
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: InitViewModel = hiltViewModel()
            val alwaysDark by viewModel.getBooleanPreference(key = SettingsRepository.BooleanPreferenceType.DARK_MODE)
                .collectAsState(initial = null)
            val dynamicColor by viewModel.getBooleanPreference(key = SettingsRepository.BooleanPreferenceType.DYNAMIC_COLOR)
                .collectAsState(initial = null)

            enableEdgeToEdge()

            if (alwaysDark != null && dynamicColor != null) {
                LiteraryLincTheme(
                    alwaysDark = alwaysDark!!,
                    dynamicColor = dynamicColor!!,
                    content = {
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

@HiltViewModel
class InitViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
    ViewModel() {
    fun getBooleanPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }
}