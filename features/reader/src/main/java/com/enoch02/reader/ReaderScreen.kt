package com.enoch02.reader

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.DocumentActivity
import com.enoch02.database.model.Document
import com.enoch02.reader.components.PdfListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val contentState = viewModel.contentState
    val context = LocalContext.current
    val pdfFiles = viewModel.documents.collectAsState(initial = emptyList())
    val covers = viewModel.covers.collectAsState(initial = emptyMap())

    var isDirectoryPicked by rememberSaveable { mutableStateOf(false) }
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.pdfDirectory = uri

                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                viewModel.savePickedDirectoryUri(context, uri)
                isDirectoryPicked = true
            }
        }
    }
    val documentViewerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            // Extract data from the result
            //val data = activityResult.data?.getStringExtra("resultKey")
            //result = data
        }
    }

    val isRefreshing = viewModel.isRefreshing
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(
        key1 = Unit,
        block = {
            isDirectoryPicked = viewModel.isDirectoryPickedBefore(context)
            if (isDirectoryPicked) {
                viewModel.loadContent(context)
            }
        }
    )

    SideEffect {
        if (viewModel.isDirectoryPickedBefore(context) && pdfFiles.value.isEmpty()) {
            viewModel.loadContent(context)
        }
    }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refreshing(context)
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullRefreshState.startRefresh()
        } else {
            pullRefreshState.endRefresh()
        }
    }


    Box(modifier = modifier) {
        ReaderList(
            isDirectoryPicked = isDirectoryPicked,
            contentState = contentState,
            documents = pdfFiles.value,
            covers = covers.value,
            directoryPickerLauncher = directoryPickerLauncher,
            modifier = Modifier.nestedScroll(pullRefreshState.nestedScrollConnection),
            onItemClicked = { item ->
                val intent = Intent(context, DocumentActivity::class.java)
                    .apply {
                        action = Intent.ACTION_VIEW
                        data = item.contentUri
                    }
                documentViewerLauncher.launch(intent)
            }
        )

        PullToRefreshContainer(
            modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState
        )
    }
}

@Composable
fun ReaderList(
    isDirectoryPicked: Boolean,
    contentState: ContentState,
    documents: List<Document>,
    covers: Map<String, String?>,
    directoryPickerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier,
    onItemClicked: (item: Document) -> Unit,
) {
    if (isDirectoryPicked) {
        AnimatedContent(
            targetState = contentState,
            content = {
                when (it) {
                    ContentState.Loading -> {
                        Box(
                            modifier = modifier.fillMaxSize(),
                            content = {
                                CircularProgressIndicator()
                            },
                            contentAlignment = Alignment.Center
                        )
                    }

                    ContentState.Success -> {
                        LazyColumn(
                            content = {
                                items(documents) { item ->
                                    PdfListItem(
                                        name = item.name,
                                        cover = covers[item.cover],
                                        onClick = {
                                            onItemClicked(item)
                                        }
                                    )
                                }
                            },
                            modifier = modifier.fillMaxWidth()
                        )
                    }

                    ContentState.Error -> TODO()
                }
            }, label = ""
        )
    } else {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = {
                Text("Select or create a PDF directory")
                Spacer(Modifier.height(4.dp))
                Button(
                    content = {
                        Text("Select")
                    },
                    onClick = {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        directoryPickerLauncher.launch(intent)
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        )
    }
}
