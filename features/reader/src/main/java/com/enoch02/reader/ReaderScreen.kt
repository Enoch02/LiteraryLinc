package com.enoch02.reader

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.artifex.mupdf.viewer.DocumentActivity
import com.enoch02.database.model.Document
import com.enoch02.reader.components.PdfListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    modifier: Modifier,
    viewModel: ReaderViewModel = hiltViewModel(),
    onScanForDocs: () -> Unit
) {
    val context = LocalContext.current
    val pdfFiles = viewModel.documents.collectAsState(initial = emptyList())
    val covers = viewModel.covers.collectAsState(initial = emptyMap())

    var isDirectoryPicked by rememberSaveable { mutableStateOf(false) }
    val documentViewerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            // Extract data from the result
            //val data = activityResult.data?.getStringExtra("resultKey")
            //result = data
        }
    }

    LaunchedEffect(
        key1 = Unit,
        block = {
            isDirectoryPicked = viewModel.isDirectoryPickedBefore(context)
        }
    )

    if (isDirectoryPicked) {
        ReaderList(
            documents = pdfFiles.value,
            covers = covers.value,
            modifier = modifier,
            onItemClicked = { item ->
                val intent = Intent(context, DocumentActivity::class.java)
                    .apply {
                        action = Intent.ACTION_VIEW
                        data = item.contentUri
                    }
                documentViewerLauncher.launch(intent)
            },
            onScanForDocs = {
                onScanForDocs()
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

@Composable
fun ReaderList(
    documents: List<Document>,
    covers: Map<String, String?>,
    modifier: Modifier = Modifier,
    onItemClicked: (item: Document) -> Unit,
    onScanForDocs: () -> Unit
) {
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
}
