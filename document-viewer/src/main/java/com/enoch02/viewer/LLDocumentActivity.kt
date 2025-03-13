package com.enoch02.viewer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.R
import com.enoch02.resources.theme.LiteraryLincTheme
import com.enoch02.viewer.components.DocumentView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LLDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        setContent {
            val viewModel: LLDocumentViewModel = hiltViewModel()
            val dynamicColor by viewModel.dynamicColor.collectAsState(false)

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
                    dynamicColor = dynamicColor,
                    content = {
                        if (uri == null) {
                            Text("Cannot open Document")
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                content = {
                                    DocumentView(
                                        uri = uri,
                                        mimeType = mimeType,
                                        documentId = documentId,
                                        viewModel = viewModel,
                                        closeViewAction = {
                                            finish()
                                        }
                                    )

                                    if (viewModel.requiresPassword) {
                                        AlertDialog(
                                            title = {
                                                Text(stringResource(R.string.password_required))
                                            },
                                            text = {
                                                OutlinedTextField(
                                                    value = viewModel.password,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                                    visualTransformation = PasswordVisualTransformation(),
                                                    onValueChange = {
                                                        viewModel.password = it
                                                    }
                                                )
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.authenticate(this@LLDocumentActivity)
                                                    },
                                                    content = {
                                                        Text(stringResource(R.string.ok))
                                                    }
                                                )
                                            },
                                            dismissButton = {
                                                TextButton(
                                                    onClick = {
                                                        finish()
                                                    },
                                                    content = {
                                                        Text(stringResource(R.string.cancel))
                                                    }
                                                )
                                            },
                                            onDismissRequest = {}
                                        )
                                    }
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}