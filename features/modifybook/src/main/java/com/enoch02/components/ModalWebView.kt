package com.enoch02.components

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalWebView(visible: Boolean, url: String, onDismiss: () -> Unit) {
    var loading by rememberSaveable { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            content = {
                if (loading) {
                    LinearProgressIndicator(progress / 100, modifier = Modifier.fillMaxWidth())
                }

                AndroidView(
                    factory = {
                        WebView(it).apply {
                            settings.javaScriptEnabled = true

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    loading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    loading = false
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    progress = newProgress.toFloat()
                                }
                            }

                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        /*it.loadUrl(url)*/
                        webView.setOnLongClickListener {
                            it.showContextMenu()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        )
    }
}