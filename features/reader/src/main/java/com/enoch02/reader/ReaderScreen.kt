package com.enoch02.reader

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.DocumentActivity
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.database.model.ReaderFilter
import com.enoch02.database.model.ReaderSorting
import com.enoch02.reader.components.ReaderListItem
import java.time.Instant
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ReaderScreen"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderScreen(
    modifier: Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
    sorting: ReaderSorting,
    filter: ReaderFilter,
    onScanForDocs: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val state = rememberScrollAreaState(listState)
    val documents by viewModel.getDocuments(context, sorting, filter)
        .collectAsState(initial = emptyList())
    val covers by viewModel.covers.collectAsState(initial = emptyMap())
    var currentDocumentIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val documentViewerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                try {
                    val currentDocument = documents[currentDocumentIndex]
                    val pages = activityResult.data?.getIntExtra("pages", 0)
                    val currentPage =
                        activityResult.data?.getIntExtra("currentPage", 0)
                    val lastRead =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Date.from(Instant.now())
                        } else {
                            Calendar.getInstance().time
                        }
                    val modifiedDocument = currentDocument.copy(
                        pages = pages ?: 0,
                        currentPage = currentPage ?: 0,
                        lastRead = lastRead,
                        isRead = currentPage == pages
                    )

                    viewModel.updateDocumentInfo(modifiedDocument)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e(TAG, "ReaderScreen: could not update document reader list")
                }
            }
        }
    )

    if (documents.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                when (filter) {
                    ReaderFilter.READING -> {
                        Text(text = stringResource(R.string.nothing_to_continue))
                    }

                    ReaderFilter.FAVORITES -> {
                        Text(text = stringResource(R.string.no_favorite_docs))
                    }

                    ReaderFilter.COMPLETED -> {
                        Text(text = stringResource(R.string.no_completed_docs))
                    }

                    ReaderFilter.ALL -> {
                        Text(text = stringResource(R.string.no_doc_found))
                        Button(
                            onClick = onScanForDocs,
                            content = {
                                Text(text = stringResource(R.string.scan_for_new_docs))
                            }
                        )
                    }

                    ReaderFilter.NO_FILE -> {
                        Text(text = stringResource(R.string.no_doc_found))
                    }
                }
            }
        )
    } else {
        ScrollArea(
            state = state,
            modifier = modifier,
            content = {
                LazyColumn(
                    state = listState,
                    content = {
                        items(
                            items = documents,
                            itemContent = { document ->
                                val inBookList by viewModel.isDocumentInBookList(document.id)
                                    .collectAsState(false)

                                ReaderListItem(
                                    document = document,
                                    documentInBookList = inBookList,
                                    cover = covers[document.cover],
                                    onClick = {
                                        currentDocumentIndex = documents.indexOf(document)
                                        viewModel.createBookListEntry(document)

                                        val intent = Intent(context, DocumentActivity::class.java)
                                            .apply {
                                                action = Intent.ACTION_VIEW
                                                data = document.contentUri
                                            }

                                        documentViewerLauncher.launch(intent)
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
                                        val intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_STREAM, document.contentUri)
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
                                    },
                                    onDeleteDocument = {
                                        viewModel.deleteDocument(
                                            context = context,
                                            document = document
                                        )
                                    }
                                )

                                if (documents.indexOf(document) != documents.lastIndex) {
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
    }
}
