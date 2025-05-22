package com.enoch02.reader

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.database.model.LLDocument
import com.enoch02.database.model.ReaderFilter
import com.enoch02.database.model.ReaderSorting
import com.enoch02.reader.components.NoDocumentView
import com.enoch02.reader.components.ReaderListItem
import com.enoch02.reader.components.ReaderSearchBottomSheet
import com.enoch02.resources.LLString
import com.enoch02.viewer.LLDocumentActivity
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun ReaderListScreen(
    modifier: Modifier,
    viewModel: ReaderListViewModel = hiltViewModel(),
    sorting: ReaderSorting,
    filter: ReaderFilter,
    isSearching: Boolean,
    onScanForDocs: () -> Unit,
    onDismissSearching: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val arrangedDocs by viewModel.documentsState.collectAsStateWithLifecycle()
    val covers by viewModel.covers.collectAsState(initial = emptyMap())
    val listState = rememberLazyListState()
    val scrollAreaState = rememberScrollAreaState(listState)
    val coroutineScope = rememberCoroutineScope()
    var scrollToTheTippyTop by rememberSaveable { mutableStateOf(false) }

    val listItemClicked = { document: LLDocument ->
        scrollToTheTippyTop = true
        viewModel.createBookListEntry(document)

        val intent =
            Intent(context, LLDocumentActivity::class.java)
                .apply {
                    action = Intent.ACTION_VIEW
                    data = document.contentUri
                    putExtra("id", document.id)
                }
        context.startActivity(intent)
    }
    val shareDocument = { document: LLDocument ->
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                document.contentUri
            )
            type = document.contentUri?.let { uri ->
                context.contentResolver.getType(
                    uri
                )
            }
        }

        context.startActivity(
            Intent.createChooser(
                intent,
                context.getString(
                    LLString.chooserTitle
                )
            )
        )
    }

    LaunchedEffect(sorting, filter) {
        scrollToTheTippyTop = true
        viewModel.updateFilter(filter)
        viewModel.updateSorting(sorting)
    }

    SideEffect {
        if (scrollToTheTippyTop) {
            coroutineScope.launch {
                listState.scrollToItem(0)
                if (listState.firstVisibleItemIndex == 0) {
                    scrollToTheTippyTop = false
                }
            }
        }
    }

    when (val docList = arrangedDocs) {
        is DocumentsState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                content = { CircularProgressIndicator() }
            )
        }

        is DocumentsState.Loaded -> {
            if (docList.documents.isEmpty()) {
                NoDocumentView(
                    filter = filter,
                    onScanForDocs = onScanForDocs
                )
            } else {
                val deletionFailedSnackbar: (message: String, document: LLDocument) -> Unit =
                    { message, document ->
                        coroutineScope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = "Remove",
                                withDismissAction = true
                            )

                            when (result) {
                                SnackbarResult.Dismissed -> {}
                                SnackbarResult.ActionPerformed -> {
                                    viewModel.deleteDocumentEntry(document)
                                }
                            }
                        }
                    }

                ScrollArea(
                    state = scrollAreaState,
                    modifier = modifier,
                    content = {
                        LazyColumn(
                            state = listState,
                            content = {
                                items(
                                    items = docList.documents,
                                    key = { item -> item.id },
                                    itemContent = { document ->
                                        val inBookList by viewModel.isDocumentInBookList(document.id)
                                            .collectAsState(false)

                                        ReaderListItem(
                                            document = document,
                                            documentInBookList = inBookList,
                                            cover = covers[document.cover],
                                            onClick = {
                                                listItemClicked(document)
                                            },
                                            onAddToFavoritesClicked = {
                                                viewModel.toggleFavoriteStatus(document)
                                            },
                                            onMarkAsReadClicked = {
                                                viewModel.toggleDocumentReadStatus(document)
                                            },
                                            onAddToBookList = {
                                                viewModel.createBookListEntry(document)
                                            },
                                            onRemoveFromBookList = {
                                                viewModel.removeBookListEntry(document.id)
                                            },
                                            onToggleAutoTracking = {
                                                viewModel.toggleDocumentAutoTracking(document)
                                            },
                                            onShare = {
                                                shareDocument(document)
                                            },
                                            onDeleteDocument = {
                                                coroutineScope.launch {
                                                    viewModel.deleteDocument(document)
                                                        .onFailure { error ->
                                                            deletionFailedSnackbar(
                                                                error.message.toString(),
                                                                document
                                                            )
                                                        }
                                                }
                                            }
                                        )

                                        if (docList.documents.indexOf(document) != docList.documents.lastIndex) {
                                            HorizontalDivider()
                                        }
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
                                        .background(Color.LightGray),
                                    thumbVisibility = ThumbVisibility.HideWhileIdle(
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        hideDelay = 0.5.seconds
                                    )
                                )
                            }
                        )
                    }
                )

                ReaderSearchBottomSheet(
                    visible = isSearching,
                    onDismiss = { onDismissSearching() },
                    onSearch = { query ->
                        viewModel.searchFor(query)
                    },
                    covers = covers,
                    isDocumentInBookList = { id -> viewModel.isDocumentInBookList(id) },
                    onItemClick = {
                        onDismissSearching()
                        listItemClicked(it)
                    },
                    onAddToFavoritesClicked = { viewModel.toggleFavoriteStatus(it) },
                    onMarkAsReadClicked = { viewModel.toggleDocumentReadStatus(it) },
                    onAddToBookList = { viewModel.createBookListEntry(it) },
                    onRemoveFromBookList = { viewModel.removeBookListEntry(it) },
                    onToggleAutoTracking = { viewModel.toggleDocumentAutoTracking(it) },
                    onDeleteDocument = {
                        coroutineScope.launch {
                            viewModel.deleteDocument(it)
                                .onFailure { error ->
                                    deletionFailedSnackbar(error.message.toString(), it)
                                }
                        }
                    },
                    onShare = { shareDocument(it) }
                )
            }
        }
    }
}
