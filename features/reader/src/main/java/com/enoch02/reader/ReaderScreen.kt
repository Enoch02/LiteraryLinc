package com.enoch02.reader

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.DocumentActivity
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.enoch02.database.model.LLDocument
import com.enoch02.reader.components.DocumentListItem
import java.lang.IndexOutOfBoundsException
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ReaderScreen"

@Composable
fun ReaderScreen(
    modifier: Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
    onScanForDocs: () -> Unit
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState(initial = emptyList())
    val covers by viewModel.covers.collectAsState(initial = emptyMap())

    var isDirectoryPicked by rememberSaveable { mutableStateOf(false) }
    var currentDocumentIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val documentViewerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                try {
                    val title = activityResult.data?.getStringExtra("title")
                    val author = activityResult.data?.getStringExtra("author")
                    val pages = activityResult.data?.getIntExtra("pages", 0)
                    val currentPage =
                        activityResult.data?.getIntExtra("currentPage", 0)
                    val currentDocument = documents[currentDocumentIndex]
                    val modifiedDocument = currentDocument.copy(
                        name = if (title.isNullOrBlank()) currentDocument.name else title,
                        author = author ?: "",
                        pages = pages ?: 0,
                        currentPage = currentPage ?: 0
                    )

                    viewModel.updateDocumentInfo(modifiedDocument)
                } catch (e: IndexOutOfBoundsException) {
                    Log.e(TAG, "ReaderScreen: could not update document reader list")
                }
            }
        }
    )

    LaunchedEffect(
        key1 = Unit,
        block = {
            isDirectoryPicked = viewModel.isDirectoryPickedBefore(context)
        }
    )

    if (isDirectoryPicked) {
        ReaderList(
            documents = documents,
            covers = covers,
            documentViewerLauncher = documentViewerLauncher,
            modifier = modifier,
            onScanForDocs = {
                onScanForDocs()
            },
            onItemClick = { index ->
                currentDocumentIndex = index
            }
        )

    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text(text = "Directory has not been selected")
                Button(
                    onClick = onScanForDocs,
                    content = {
                        Text(text = "Pick a new directory")
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReaderList(
    documents: List<LLDocument>,
    covers: Map<String, String?>,
    documentViewerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier,
    onScanForDocs: () -> Unit,
    onItemClick: (index: Int) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val state = rememberScrollAreaState(listState)

    if (documents.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text(text = "No Document found")
                Button(
                    onClick = onScanForDocs,
                    content = {
                        Text(text = "Scan for new documents")
                    }
                )
            }
        )
    } else {
        ScrollArea(
            state = state,
            content = {
                LazyColumn(
                    state = listState,
                    content = {
                        items(documents) { item ->
                            DocumentListItem(
                                document = item,
                                cover = covers[item.cover],
                                onClick = {
                                    onItemClick(documents.indexOf(item))

                                    val intent = Intent(context, DocumentActivity::class.java)
                                        .apply {
                                            action = Intent.ACTION_VIEW
                                            data = item.contentUri
                                        }

                                    documentViewerLauncher.launch(intent)
                                }
                            )

                            if (documents.indexOf(item) != documents.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    },
                    modifier = modifier.fillMaxWidth()
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
