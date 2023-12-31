package com.enoch02.literarylinc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.enoch02.literarylinc.navigation.LiteraryLincNavHost
import com.enoch02.literarylinc.ui.theme.LiteraryLincTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LiteraryLincTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(rememberNestedScrollInteropConnection()),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LiteraryLincNavHost()
                }
            }
        }
    }
}