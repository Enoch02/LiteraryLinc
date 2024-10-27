package com.artifex.mupdf.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.viewer.components.ContentState
import com.artifex.mupdf.viewer.components.ReaderBottomBar
import com.artifex.mupdf.viewer.components.ReaderTopBar
import com.enoch02.literarylinc.ui.theme.LiteraryLincTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
                    val documentId = intent.getStringExtra("id")

                    if (uri == null) {
                        Text("Cannot open Document")
                    } else {
                        ReaderView(uri = uri, mimeType = mimeType, documentId = documentId)
                    }
                }
            }
        }
    }

    @Composable
    fun ReaderView(
        viewModel: LLReaderViewModel = hiltViewModel(),
        uri: Uri,
        mimeType: String?,
        documentId: String?
    ) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            viewModel.initDocument(
                context = context,
                uri = uri,
                mimeType = mimeType,
                id = documentId
            )
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
                        val coroutineScope = rememberCoroutineScope()
                        var showBars by rememberSaveable {
                            mutableStateOf(true)
                        }
                        val pageCount = viewModel.pages.size
                        val pagerState = rememberPagerState(
                            pageCount = { pageCount },
                            initialPage = viewModel.currentPage
                        )

                        LaunchedEffect(pagerState) {
                            // Collect the current page index whenever it changes
                            snapshotFlow { pagerState.currentPage }.collectLatest { currentPage ->
                                viewModel.currentPage = currentPage + 1
                                viewModel.updateDocumentData()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = {
                                HorizontalPager(
                                    state = pagerState,
                                    /*beyondViewportPageCount = 3,*/
                                    pageContent = { index ->
                                        var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }

                                        LaunchedEffect(index) {
                                            viewModel.getPageBitmap(index).collect { bitmap ->
                                                pageBitmap = bitmap
                                            }
                                        }

                                        if (pageBitmap != null) {
                                            AsyncImage(
                                                model = pageBitmap,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .pointerInput(Unit) {
                                                        detectTapGestures { offset ->
                                                            // Get the total size of the composable
                                                            val size = this.size
                                                            val width = size.width
                                                            val height = size.height

                                                            // Define center region (middle third of the screen)
                                                            val horizontalThird = width / 3
                                                            val verticalThird = height / 3

                                                            // Check if tap is in center region
                                                            val isInCenterX =
                                                                offset.x >= horizontalThird && offset.x <= horizontalThird * 2
                                                            val isInCenterY =
                                                                offset.y >= verticalThird && offset.y <= verticalThird * 2

                                                            if (isInCenterX && isInCenterY) {
                                                                showBars = !showBars
                                                            }
                                                        }
                                                    }
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                                content = { CircularProgressIndicator() }
                                            )
                                        }
                                    }
                                )

                                val toast = Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT)

                                ReaderTopBar(
                                    modifier = Modifier.align(Alignment.TopCenter),
                                    visible = showBars,
                                    documentTitle = viewModel.document?.getMetaData(Document.META_INFO_TITLE)
                                        ?: viewModel.docTitle,
                                    onLink = {
                                        toast.show()
                                    },
                                    onSearch = {
                                        toast.show()
                                    },
                                    onOutline = {
                                        toast.show()
                                        //TODO: open the outline composable from here
                                    }
                                )

                                if (pageCount > 0) {
                                    ReaderBottomBar(
                                        modifier = Modifier.align(Alignment.BottomCenter),
                                        visible = showBars,
                                        currentPage = viewModel.currentPage,
                                        pageCount = pageCount,
                                        onPageChange = { newPageIndex ->
                                            coroutineScope.launch {
                                                pagerState.scrollToPage(newPageIndex.toInt())
                                            }
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
