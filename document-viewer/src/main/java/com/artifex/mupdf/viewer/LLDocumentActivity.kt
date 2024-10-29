package com.artifex.mupdf.viewer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.artifex.mupdf.fitz.Document
import com.artifex.mupdf.viewer.components.ContentState
import com.artifex.mupdf.viewer.components.ReaderBottomBar
import com.artifex.mupdf.viewer.components.ReaderTopBar
import com.artifex.mupdf.viewer.shared.Item
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
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
            MaterialTheme {
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
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                        LaunchedEffect(pagerState) {
                            // Collect the current page index whenever it changes
                            snapshotFlow { pagerState.currentPage }.collectLatest { currentPage ->
                                viewModel.currentPage = currentPage
                                viewModel.updateDocumentData()
                            }
                        }

                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = viewModel.hasOutline,
                            drawerContent = {
                                TableOfContentSheet(
                                    chapterPage = /*viewModel.getChapterStart()*/pagerState.currentPage,
                                    outline = viewModel.flatOutline,
                                    onItemSelected = { page ->
                                        coroutineScope.launch {
                                            pagerState.scrollToPage(page)
                                            drawerState.close()
                                        }
                                    }
                                )
                            },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                    content = {
                                        HorizontalPager(
                                            state = pagerState,
                                            pageContent = { index ->
                                                var pageBitmap by remember {
                                                    mutableStateOf<Bitmap?>(
                                                        null
                                                    )
                                                }

                                                LaunchedEffect(index) {
                                                    viewModel.getPageBitmap(index)
                                                        .collect { bitmap ->
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

                                        val toast =
                                            Toast.makeText(
                                                context,
                                                "Coming soon!",
                                                Toast.LENGTH_SHORT
                                            )

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
                                            hasOutline = viewModel.hasOutline,
                                            onOutline = {
                                                coroutineScope.launch {
                                                    drawerState.open()
                                                }
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
                        )
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun TableOfContentSheet(chapterPage: Int, outline: List<Item>, onItemSelected: (Int) -> Unit) {
        var selected by remember { mutableIntStateOf(0) }
        selected = outline.indexOfFirst { item -> item.page >= chapterPage }
            .takeIf { it1 -> it1 != -1 } ?: 0 // Default to 0 if not found

        ModalDrawerSheet(
            content = {
                val listState = rememberLazyListState()
                val state = rememberScrollAreaState(listState)

                /*LaunchedEffect(
                    key1 = chapterPage,
                    block = {
                        listState.scrollToItem(selected)
                    }
                )*/
                LaunchedEffect(
                    key1 = chapterPage,
                    block = {
                        val offset = 5

                        if (selected - offset > 0 && selected < outline.size) {
                            listState.animateScrollToItem(selected - 5)
                        }
                    }
                )

                ScrollArea(
                    state = state,
                    content = {
                        LazyColumn(
                            state = listState,
                            content = {
                                items(
                                    count = outline.size,
                                    itemContent = { index ->
                                        val item = outline[index]
                                        val backgroundColor = if (index == selected) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) // Tint for selected item
                                        } else {
                                            Color.Transparent // Default background
                                        }

                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = if (!item.title.startsWith(" ")) FontWeight.Bold else FontWeight.Normal,
                                                    modifier = Modifier.align(Alignment.TopStart)
                                                )
                                            },
                                            trailingContent = { Text(text = "${item.page + 1}") },
                                            colors = ListItemDefaults.colors(containerColor = backgroundColor),
                                            modifier = Modifier
                                                .clickable {
                                                    onItemSelected(item.page)
                                                }
                                        )
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .fillMaxHeight()
                                .width(8.dp),
                            thumb = {
                                Thumb(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.LightGray)
                                )
                            }
                        )
                    }
                )
            }
        )
    }
}
