package com.enoch02.viewer.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.FileUriExposedException
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import com.artifex.mupdf.fitz.Link
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.settings.SettingsRepository
import com.enoch02.viewer.LLReaderViewModel
import com.enoch02.viewer.model.ContentState
import com.enoch02.viewer.model.Item
import com.enoch02.viewer.model.SearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ReaderView(
    viewModel: LLReaderViewModel,
    uri: Uri,
    mimeType: String?,
    documentId: String?
) {
    val context = LocalContext.current
    val volumePaging by viewModel.getPreference(SettingsRepository.BooleanPreferenceType.VOLUME_BTN_PAGING)
        .collectAsState(initial = false)

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
        label = ""
    ) { state ->
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
                val searchResults = viewModel.searchResults
                val links = viewModel.documentLinks

                LaunchedEffect(pagerState) {
                    // Collect the current page index whenever it changes
                    snapshotFlow { pagerState.currentPage }.collectLatest { currentPage ->
                        viewModel.currentPage = currentPage
                        viewModel.updateDocumentData()
                    }
                }

                if (volumePaging) {
                    VolumeButtonDetector(
                        onVolumeUp = {
                            coroutineScope.launch {
                                if (viewModel.currentPage < pageCount) {
                                    pagerState.scrollToPage(viewModel.currentPage + 1)
                                }
                            }
                        },
                        onVolumeDown = {
                            coroutineScope.launch {
                                if (viewModel.currentPage > 1) {
                                    pagerState.scrollToPage(viewModel.currentPage - 1)
                                }
                            }
                        }
                    )
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = viewModel.hasOutline,
                    drawerContent = {
                        TableOfContentSheet(
                            chapterPage = pagerState.currentPage,
                            outline = viewModel.flatOutline,
                            onItemSelected = { page ->
                                coroutineScope.launch {
                                    pagerState.scrollToPage(page)
                                    drawerState.close()
                                }
                            }
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(state = pagerState, beyondViewportPageCount = 1) { index ->
                            var pageBitmap by remember {
                                mutableStateOf<Bitmap?>(
                                    null
                                )
                            }
                            var pageContainerSize by remember {
                                mutableStateOf(
                                    IntSize.Zero
                                )
                            }
                            var pageZoom by remember { mutableFloatStateOf(1f) }
                            val debouncedZoom by rememberDebouncedState(pageZoom)

                            LaunchedEffect(index, debouncedZoom) {
                                viewModel.getPageBitmap(index, debouncedZoom)
                                    .collect { bitmap ->
                                        pageBitmap = bitmap
                                    }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clipToBounds(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (pageBitmap != null) {
                                    SubcomposeAsyncImage(
                                        model = pageBitmap,
                                        contentDescription = null,
                                        filterQuality = FilterQuality.High,
                                        modifier = Modifier
                                            .onGloballyPositioned { coordinates ->
                                                pageContainerSize = coordinates.size
                                            }
                                            .graphicsLayer(
                                                scaleX = pageZoom,
                                                scaleY = pageZoom,
                                                transformOrigin = TransformOrigin(0.5f, 0.5f),
                                            )
                                            .pointerInput(Unit) {
                                                awaitEachGesture {
                                                    // Wait for first touch
                                                    awaitFirstDown(requireUnconsumed = false)
                                                    do {
                                                        val event = awaitPointerEvent()
                                                        if (event.changes.size >= 2) {
                                                            val zoomChange = event.calculateZoom()
                                                            pageZoom =
                                                                (pageZoom * zoomChange).coerceIn(1f..3f)

                                                            // Consume the events when we're handling multi-touch
                                                            event.changes.forEach { it.consume() }
                                                        }
                                                    } while (event.changes.any { it.pressed })
                                                }
                                            }
                                            .pointerInput(Unit) {
                                                detectTapGestures { tapOffset ->
                                                    val size = this.size
                                                    val width = size.width
                                                    val height = size.height

                                                    val horizontalThird = width / 3
                                                    val verticalThird = height / 3

                                                    val isInCenterX =
                                                        tapOffset.x >= horizontalThird && tapOffset.x <= horizontalThird * 2
                                                    val isInCenterY =
                                                        tapOffset.y >= verticalThird && tapOffset.y <= verticalThird * 2

                                                    if (isInCenterX && isInCenterY) {
                                                        showBars = !showBars
                                                    }
                                                }
                                            }
                                    ) {
                                        val painterState = painter.state
                                        val pageBounds =
                                            viewModel.getPageBounds(index)

                                        if (painterState is AsyncImagePainter.State.Success) {
                                            Image(
                                                painter = painterState.painter,
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .background(Color.White)
                                            )
                                        }

                                        if (viewModel.showSearchResults) {
                                            pageContainerSize.let { size ->
                                                val pageSize = IntSize(
                                                    width = pageBounds.first,
                                                    height = pageBounds.second
                                                )

                                                SearchHighlights(
                                                    searchResults = searchResults.filter { it.pageNumber == index },
                                                    originalPageSize = pageSize,
                                                    containerSize = size,
                                                    highlightColor = Color.Yellow.copy(
                                                        alpha = 0.7f
                                                    )
                                                )
                                            }
                                        }

                                        if (viewModel.showLinks) {
                                            val pageLinks =
                                                links.find { it.page == index }

                                            pageContainerSize.let { size ->
                                                pageLinks?.links?.let {
                                                    DocumentLinkOverlay(
                                                        links = it,
                                                        originalPageSize = IntSize(
                                                            width = pageBounds.first,
                                                            height = pageBounds.second
                                                        ),
                                                        containerSize = size,
                                                        onLinkClick = { link ->
                                                            if (link.isExternal) {
                                                                val intent =
                                                                    Intent(
                                                                        Intent.ACTION_VIEW,
                                                                        Uri.parse(link.uri)
                                                                    )

                                                                try {
                                                                    context.startActivity(intent)
                                                                } catch (x: FileUriExposedException) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Android does not allow following file:// link: " + link.uri,
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                } catch (x: Throwable) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        x.message,
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                            } else {
                                                                coroutineScope.launch {
                                                                    val pageNum =
                                                                        viewModel.getPageIndexFromLink(
                                                                            link
                                                                        )

                                                                    pageNum?.let { num ->
                                                                        pagerState.scrollToPage(
                                                                            num
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        ReaderTopBar(
                            modifier = Modifier.align(Alignment.TopCenter),
                            visible = showBars,
                            documentTitle = viewModel.getDocumentTitle(),
                            showLinks = viewModel.showLinks,
                            onLink = {
                                viewModel.toggleShowLinks()
                            },
                            searchQuery = viewModel.searchQuery,
                            onSearch = {
                                viewModel.startSearch(
                                    noResultAction = {
                                        Toast.makeText(
                                            context,
                                            "No match has been found",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            },
                            onSearchQueryChange = { newQuery ->
                                viewModel.searchQuery = newQuery
                            },
                            onPreviousSearchResult = {
                                coroutineScope.launch {
                                    viewModel.moveToPreviousSearchResult()
                                        ?.let { pagerState.scrollToPage(it) }
                                }
                            },
                            onNextSearchResult = {
                                coroutineScope.launch {
                                    viewModel.moveToNextSearchResult()
                                        ?.let { pagerState.scrollToPage(it) }
                                }
                            },
                            onHideResults = {
                                viewModel.showSearchResults = false
                            },
                            searchInProgress = viewModel.searchInProgress,
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
                                        pagerState.scrollToPage(newPageIndex)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHighlights(
    searchResults: List<SearchResult>,
    originalPageSize: IntSize,
    containerSize: IntSize,
    highlightColor: Color = Color.Yellow.copy(alpha = 0.3f)
) {
    Canvas(
        modifier = Modifier.fillMaxSize(),
        onDraw = {
            // Calculate scale to match the rendered PDF size
            val containerAspect = containerSize.width.toFloat() / containerSize.height
            val pageAspect = originalPageSize.width.toFloat() / originalPageSize.height

            val (renderWidth, renderHeight, xOffset, yOffset) = if (containerAspect > pageAspect) {
                // Height constrained
                val height = containerSize.height.toFloat()
                val width = height * pageAspect
                val xOffset = (containerSize.width - width) / 2f
                listOf(width, height, xOffset, 0f)
            } else {
                // Width constrained
                val width = containerSize.width.toFloat()
                val height = width / pageAspect
                val yOffset = (containerSize.height - height) / 2f
                listOf(width, height, 0f, yOffset)
            }

            val scaleX = renderWidth / originalPageSize.width
            val scaleY = renderHeight / originalPageSize.height

            searchResults.forEach { result ->
                result.quads.forEach { quad ->
                    val path = Path().apply {
                        moveTo(
                            quad.ul_x * scaleX + xOffset,
                            quad.ul_y * scaleY + yOffset
                        )
                        lineTo(
                            quad.ur_x * scaleX + xOffset,
                            quad.ur_y * scaleY + yOffset
                        )
                        lineTo(
                            quad.lr_x * scaleX + xOffset,
                            quad.lr_y * scaleY + yOffset
                        )
                        lineTo(
                            quad.ll_x * scaleX + xOffset,
                            quad.ll_y * scaleY + yOffset
                        )
                        close()
                    }
                    drawPath(
                        path = path,
                        color = highlightColor
                    )
                }
            }
        }
    )
}

@Composable
fun DocumentLinkOverlay(
    links: Array<Link>,
    originalPageSize: IntSize,
    containerSize: IntSize,
    linkColor: Color = Color(0x220000FF),
    onLinkClick: (Link) -> Unit
) {
    // Remember calculated link areas to avoid recalculation on each tap
    val linkAreas = remember(links, containerSize, originalPageSize) {
        calculateLinkAreas(links, originalPageSize, containerSize)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(links) {
                detectTapGestures { offset ->
                    // Find which link was clicked
                    linkAreas.forEach { (link, rect) ->
                        if (rect.contains(offset)) {
                            onLinkClick(link)
                            return@detectTapGestures
                        }
                    }
                }
            },
        onDraw = {
            val containerAspect = containerSize.width.toFloat() / containerSize.height
            val pageAspect = originalPageSize.width.toFloat() / originalPageSize.height

            val (renderWidth, renderHeight, xOffset, yOffset) = if (containerAspect > pageAspect) {
                val height = containerSize.height.toFloat()
                val width = height * pageAspect
                val xOffset = (containerSize.width - width) / 2f
                listOf(width, height, xOffset, 0f)
            } else {
                val width = containerSize.width.toFloat()
                val height = width / pageAspect
                val yOffset = (containerSize.height - height) / 2f
                listOf(width, height, 0f, yOffset)
            }

            val scaleX = renderWidth / originalPageSize.width
            val scaleY = renderHeight / originalPageSize.height

            links.forEach { link ->
                val bounds = link.bounds
                val path = Path().apply {
                    moveTo(
                        bounds.x0 * scaleX + xOffset,
                        bounds.y0 * scaleY + yOffset
                    )
                    lineTo(
                        bounds.x1 * scaleX + xOffset,
                        bounds.y0 * scaleY + yOffset
                    )
                    lineTo(
                        bounds.x1 * scaleX + xOffset,
                        bounds.y1 * scaleY + yOffset
                    )
                    lineTo(
                        bounds.x0 * scaleX + xOffset,
                        bounds.y1 * scaleY + yOffset
                    )
                    close()
                }

                drawPath(
                    path = path,
                    color = linkColor,
                    style = Stroke(width = 1.5f)
                )
            }
        }
    )
}

private fun calculateLinkAreas(
    links: Array<Link>,
    originalPageSize: IntSize,
    containerSize: IntSize
): List<Pair<Link, Rect>> {
    val containerAspect = containerSize.width.toFloat() / containerSize.height
    val pageAspect = originalPageSize.width.toFloat() / originalPageSize.height

    val (renderWidth, renderHeight, xOffset, yOffset) = if (containerAspect > pageAspect) {
        val height = containerSize.height.toFloat()
        val width = height * pageAspect
        val xOffset = (containerSize.width - width) / 2f
        listOf(width, height, xOffset, 0f)
    } else {
        val width = containerSize.width.toFloat()
        val height = width / pageAspect
        val yOffset = (containerSize.height - height) / 2f
        listOf(width, height, 0f, yOffset)
    }

    val scaleX = renderWidth / originalPageSize.width
    val scaleY = renderHeight / originalPageSize.height

    return links.map { link ->
        val bounds = link.bounds
        val rect = Rect(
            left = bounds.x0 * scaleX + xOffset,
            top = bounds.y0 * scaleY + yOffset,
            right = bounds.x1 * scaleX + xOffset,
            bottom = bounds.y1 * scaleY + yOffset
        )
        link to rect
    }
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

@Composable
fun VolumeButtonDetector(
    onVolumeUp: () -> Unit = {},
    onVolumeDown: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current

    var volumeUpPressed = false
    var volumeDownPressed = false

    DisposableEffect(context) {
        val keyEventDispatcher = ViewCompat.OnUnhandledKeyEventListenerCompat { _, event ->
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (event.action == KeyEvent.ACTION_DOWN && !volumeUpPressed) {
                        onVolumeUp()
                        volumeUpPressed = true
                    } else if (event.action == KeyEvent.ACTION_UP) {
                        volumeUpPressed = false
                    }
                    true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (event.action == KeyEvent.ACTION_DOWN && !volumeDownPressed) {
                        onVolumeDown()
                        volumeDownPressed = true
                    } else if (event.action == KeyEvent.ACTION_UP) {
                        volumeDownPressed = false
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }

        ViewCompat.addOnUnhandledKeyEventListener(view, keyEventDispatcher)

        onDispose {
            ViewCompat.removeOnUnhandledKeyEventListener(view, keyEventDispatcher)
        }
    }
}

@Composable
fun rememberDebouncedState(
    value: Float,
    delayMillis: Long = 300L
): State<Float> {
    val debouncedValue = remember { mutableFloatStateOf(value) }

    LaunchedEffect(value) {
        delay(delayMillis)
        debouncedValue.floatValue = value
    }

    return debouncedValue
}