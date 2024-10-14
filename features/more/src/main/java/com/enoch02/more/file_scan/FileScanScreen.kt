package com.enoch02.more.file_scan

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.more.components.SwitchSettingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScanScreen(navController: NavController, viewModel: FileScanViewModel = hiltViewModel()) {
    // TODO: save using settingsViewModel
    var autoStartScans by rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    var isDirectoryPicked by rememberSaveable { mutableStateOf(false) }
    var showRemovalDialog by remember {
        mutableStateOf(false)
    }
    val fileScanInfo by viewModel.fileScanWorkInfo.collectAsState()
    val coverScanInfo by viewModel.coverScanWorkInfo.collectAsState()
    val isScanningFiles = viewModel.isScanningFiles(fileScanInfo)
    val isScanningCovers = viewModel.isScanningCovers(coverScanInfo)

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.documentDirectory = uri

                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                viewModel.savePickedDirectoryUri(context, uri)
                isDirectoryPicked = true
                viewModel.getPersistedDirectories(context)
                viewModel.loadDocuments(context, isScanningFiles, isScanningCovers)
            }
        }
    }
    val otherDirectoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                viewModel.getPersistedDirectories(context)
                viewModel.loadDocuments(context, isScanningFiles, isScanningCovers)
            }
        }
    }
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

    SideEffect {
        isDirectoryPicked = viewModel.isDirectoryPickedBefore(context)
        viewModel.getPersistedDirectories(context)
        viewModel.collectWorks()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "File scan") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            Column(
                content = {
                    AnimatedVisibility(visible = isScanningFiles || isScanningCovers) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    LazyColumn(
                        content = {
                            item {
                                ListItem(
                                    overlineContent = { Text(text = "Scanning") },
                                    headlineContent = {
                                        Card {
                                            SwitchSettingItem(
                                                label = "Autostart Scans",
                                                checked = autoStartScans,
                                                onCheckChanged = {
                                                    autoStartScans = it
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        content = {
                                                            Text(text = "Total documents: ")
                                                            Text(text = viewModel.totalDocuments.toString())
                                                        }
                                                    )
                                                },
                                                tonalElevation = 30.dp,
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = "Start Scan")
                                                },
                                                trailingContent = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Search,
                                                        contentDescription = "Start Scan"
                                                    )
                                                },
                                                tonalElevation = 30.dp,
                                                modifier = Modifier.clickable {
                                                    viewModel.loadDocuments(
                                                        context,
                                                        isScanningFiles,
                                                        isScanningCovers
                                                    )
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = "Reload covers")
                                                },
                                                supportingContent = {
                                                    Text(text = "Try to load covers for documents with missing covers")
                                                },
                                                trailingContent = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Image,
                                                        contentDescription = "Reload missing covers"
                                                    )
                                                },
                                                tonalElevation = 30.dp,
                                                modifier = Modifier.clickable {
                                                    viewModel.rescanCovers(
                                                        context,
                                                        isScanningFiles,
                                                        isScanningCovers
                                                    )
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            item {
                                ListItem(
                                    overlineContent = { Text(text = "Scan Locations") },
                                    headlineContent = {
                                        Card {
                                            ListItem(
                                                headlineContent = {
                                                    Text(text = "Select app directory")
                                                },
                                                supportingContent = {
                                                    if (isDirectoryPicked) {
                                                        Text(
                                                            text = "Current: ${viewModel.documentDirectory?.path.toString()}"
                                                        )
                                                    } else {
                                                        Text(text = "This is where the app will scan by default")
                                                    }
                                                },
                                                tonalElevation = 30.dp,
                                                modifier = Modifier.clickable {
                                                    directoryPickerLauncher.launch(intent)
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = "Select other directories")
                                                },
                                                supportingContent = {
                                                    Text(text = "Total Selected: ${viewModel.scanDirectories.size}")
                                                },
                                                tonalElevation = 30.dp,
                                                modifier = Modifier.clickable {
                                                    otherDirectoryPickerLauncher.launch(intent)
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = "Remove other directories")
                                                },
                                                tonalElevation = 30.dp,
                                                modifier = Modifier.clickable {
                                                    showRemovalDialog = true
                                                }
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    )
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )

    AnimatedVisibility(
        visible = showRemovalDialog && viewModel.scanDirectories.isNotEmpty(),
        content = {
            val scanDirs = viewModel.scanDirectories

            AlertDialog(
                onDismissRequest = { showRemovalDialog = false },
                confirmButton = {
                    TextButton(
                        content = { Text(text = "Cancel") },
                        onClick = { showRemovalDialog = false }
                    )
                },
                title = {
                    Text("Scanned Directories")
                },
                text = {
                    Card {
                        LazyColumn(
                            content = {
                                items(scanDirs.keys.toList()) { key ->
                                    ListItem(
                                        headlineContent = {
                                            Text(text = "$key", maxLines = 1)
                                        },
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    scanDirs[key]?.let {
                                                        viewModel.removePersistedFolderAccess(
                                                            context,
                                                            it
                                                        )
                                                    }
                                                    showRemovalDialog = false
                                                    viewModel.loadDocuments(
                                                        context,
                                                        isScanningFiles,
                                                        isScanningCovers
                                                    )
                                                },
                                                content = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Delete,
                                                        contentDescription = "Delete"
                                                    )
                                                }
                                            )
                                        }
                                    )

                                    if (key != scanDirs.keys.last()) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    )
}