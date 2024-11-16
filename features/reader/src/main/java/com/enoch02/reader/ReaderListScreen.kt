package com.enoch02.reader

import android.content.Intent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.core.ScrollArea
import com.composables.core.ScrollAreaState
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
import com.enoch02.viewer.LLDocumentActivity
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderListScreen(
    modifier: Modifier,
    viewModel: ReaderListViewModel = hiltViewModel(),
    sorting: ReaderSorting,
    filter: ReaderFilter,
    isSearching: Boolean,
    onScanForDocs: () -> Unit,
    onDismissSearching: () -> Unit
) {
    val context = LocalContext.current
    val arrangedDocs by viewModel.documentsState.collectAsStateWithLifecycle()
    val covers by viewModel.covers.collectAsState(initial = emptyMap())
    val listState = rememberLazyListState()
    val scrollAreaState = rememberScrollAreaState(listState)
    val coroutineScope = rememberCoroutineScope()

    val listItemClicked = { document: LLDocument ->
        coroutineScope.launch { listState.animateScrollToItem(0) }
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
                    R.string.chooser_title
                )
            )
        )
    }

    LaunchedEffect(sorting, filter) {
        viewModel.updateFilter(filter)
        viewModel.updateSorting(sorting)
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
                                                viewModel.deleteDocument(document = document)
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
                    onItemClick = { listItemClicked(it) },
                    onAddToFavoritesClicked = { viewModel.toggleFavoriteStatus(it) },
                    onMarkAsReadClicked = { viewModel.toggleDocumentReadStatus(it) },
                    onAddToBookList = { viewModel.createBookListEntry(it) },
                    onRemoveFromBookList = { viewModel.removeBookListEntry(it) },
                    onToggleAutoTracking = { viewModel.toggleDocumentAutoTracking(it) },
                    onDeleteDocument = { viewModel.deleteDocument(it) },
                    onShare = { shareDocument(it) }
                )
            }
        }
    }
}
