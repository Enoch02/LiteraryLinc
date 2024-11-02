package com.artifex.mupdf.viewer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.ui.ReaderView
import com.enoch02.resources.theme.LiteraryLincTheme
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LLDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        setContent {
            val viewModel: LLReaderViewModel = hiltViewModel()
            val dynamicColor by viewModel.getBooleanPreference(key = SettingsRepository.PreferenceType.DYNAMIC_COLOR)
                .collectAsState(initial = null)

            DisposableEffect(Unit) {
                this@LLDocumentActivity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose {
                    this@LLDocumentActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            if (Intent.ACTION_VIEW == intent.action) {
                val uri = intent.data
                val mimeType = getIntent().type
                val documentId = intent.getStringExtra("id")

                LiteraryLincTheme(
                    alwaysDark = true,
                    dynamicColor = dynamicColor ?: false,
                    content = {
                        if (uri == null) {
                            Text("Cannot open Document")
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    ReaderView(
                                        uri = uri,
                                        mimeType = mimeType,
                                        documentId = documentId,
                                        viewModel = viewModel
                                    )
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
