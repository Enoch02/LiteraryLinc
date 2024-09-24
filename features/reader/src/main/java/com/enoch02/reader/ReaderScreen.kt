package com.enoch02.reader

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.artifex.mupdf.viewer.DocumentActivity
import com.enoch02.pdfrendering.PDFBitmapConverter
import com.enoch02.reader.components.PdfListItem
import com.enoch02.reader.models.PdfFile

@Composable
fun ReaderScreen(
    modifier: Modifier,
    navController: NavController,
    viewModel: ReaderViewModel = viewModel(),
) {
    val FILE_REQUEST = 42
    val contentState = viewModel.contentState
    val context = LocalContext.current
    val pdfFiles = viewModel.pdfFiles

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

    var showViewer by rememberSaveable { mutableStateOf(false) }
    var currentPdfFile by rememberSaveable { mutableStateOf<PdfFile?>(null) }

    LaunchedEffect(
        key1 = Unit,
        block = {
            isDirectoryPicked = viewModel.isDirectoryPickedBefore(context)
            if (isDirectoryPicked) {
                viewModel.loadContent(context)
            }
        }
    )

    Box(
        modifier = modifier.fillMaxSize(),
        content = {
            ReaderList(
                isDirectoryPicked = isDirectoryPicked,
                contentState = contentState,
                pdfFiles = pdfFiles,
                directoryPickerLauncher = directoryPickerLauncher,
                modifier = Modifier,
                onItemClicked = { item ->
                    val intent = Intent(context, DocumentActivity::class.java)
                        .apply {
                            action = Intent.ACTION_VIEW
                            data = item.contentUri
                        }
                    documentViewerLauncher.launch(intent)
                    /*currentPdfFile = item
                    showViewer = true*/
                }
            )


            if (showViewer) {
                currentPdfFile?.let {
                    PdfViewer(
                        pdfFile = it,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    )
                }
            }
        }
    )

    BackHandler {
        if (showViewer) {
            showViewer = false
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReaderList(
    isDirectoryPicked: Boolean,
    contentState: ContentState,
    pdfFiles: List<PdfFile>,
    directoryPickerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier,
    onItemClicked: (item: PdfFile) -> Unit,
) {
    if (isDirectoryPicked) {
        AnimatedContent(
            targetState = contentState,
            content = {
                when (it) {
                    ContentState.Loading -> {
                        Box(
                            modifier = modifier.fillMaxSize(),
                            content = { CircularProgressIndicator() },
                            contentAlignment = Alignment.Center
                        )
                    }

                    ContentState.Success -> {
                        LazyColumn(
                            content = {
                                items(pdfFiles) { item ->
                                    PdfListItem(
                                        name = item.name,
                                        thumbnail = item.thumbnail,
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

@Composable
fun PdfViewer(pdfFile: PdfFile, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val pdfBitmapConverter = remember {
        PDFBitmapConverter(context = context)
    }
    var renderedPages by remember {
        mutableStateOf<List<Bitmap>>(emptyList())
    }

    LaunchedEffect(pdfFile) {
        renderedPages = pdfBitmapConverter.pdfToBitmaps(pdfFile.contentUri)
    }

    Column(modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                content = {
                    items(renderedPages) { page ->
                        PdfPage(page = page)
                    }
                }
            )
        }
    )
}

@Composable
fun PdfPage(page: Bitmap, modifier: Modifier = Modifier) {
    AsyncImage(
        model = page,
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(page.width.toFloat() / page.height.toFloat())
    )
}