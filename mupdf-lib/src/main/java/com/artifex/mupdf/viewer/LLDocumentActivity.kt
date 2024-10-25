package com.artifex.mupdf.viewer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.viewer.components.ContentState
import com.artifex.mupdf.viewer.components.NewPageView
import com.artifex.mupdf.viewer.components.PageView

import com.artifex.mupdf.viewer.components.ReaderBottomBar
import com.artifex.mupdf.viewer.components.ReaderTopBar
import com.enoch02.literarylinc.ui.theme.LiteraryLincTheme

class LLDocumentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent

        setContent {
            //TODO: use shared module for the theme
            LiteraryLincTheme(alwaysDark = false, dynamicColor = true) {
                if (Intent.ACTION_VIEW == intent.action) {
                    val uri = intent.data
                    val mimeType = getIntent().type

                    if (uri == null) {
                        Text("Cannot open Document")
                    } else {
                        ReaderView(uri = uri, mimeType = mimeType)
                    }
                }
            }
        }
    }

    @Composable
    fun ReaderView(viewModel: LLReaderViewModel = viewModel(), uri: Uri, mimeType: String?) {
        val context = LocalContext.current
        var showBars by rememberSaveable {
            mutableStateOf(true)
        }

        LaunchedEffect(Unit) {
            viewModel.initDocument(context = context, uri = uri, mimeType = mimeType)
        }

        AnimatedContent(
            targetState = viewModel.contentState,
            label = "",
            content = { state ->
                when (state) {
                    ContentState.LOADING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = {
                                CircularProgressIndicator()
                            }
                        )
                    }

                    ContentState.NOT_LOADING -> {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxSize(),
                            content = {
                                val pageCount =
                                    viewModel.document?.countPages() ?: 0

                                PageView(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.Center),
                                    page = viewModel.currentPageBitmap,
                                    onCenterTap = {
                                        showBars = !showBars
                                    },
                                    onSwipeRight = {
                                        viewModel.previousPage()
                                    },
                                    onSwipeLeft = {
                                        viewModel.nextPage()
                                    }
                                )
                                /*NewPageView(
                                    modifier = Modifier
                                        .align(Alignment.Center),
                                    pageCount = viewModel.document?.countPages() ?: 0,
                                    currentPage = viewModel.currentPageBitmap
                                )*/

                                ReaderTopBar(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    visible = showBars,
                                    documentTitle = viewModel.document?.getMetaData(Document.META_INFO_TITLE)
                                        ?: "",
                                    onLink = {

                                    },
                                    onSearch = {

                                    },
                                    onOutline = {
                                        //TODO: open the outline composable from here
                                    }
                                )

                                if (pageCount > 0) {
                                    ReaderBottomBar(
                                        modifier = Modifier.align(Alignment.BottomCenter),
                                        visible = showBars,
                                        currentPage = viewModel.currentPage,
                                        pageCount = pageCount,
                                        onPageChange = { newPage ->
                                            viewModel.currentPage = newPage.toInt()
                                            viewModel.getPageBitmap()
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        )
    }
}
