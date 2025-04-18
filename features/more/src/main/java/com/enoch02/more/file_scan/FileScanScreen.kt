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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.more.settings.components.DialogSettingItem
import com.enoch02.more.settings.components.SwitchSettingItem
import com.enoch02.resources.LLString
import com.enoch02.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScanScreen(navController: NavController, viewModel: FileScanViewModel = hiltViewModel()) {
    val tonalElevation = 10.dp
    val autoStartScans by viewModel.getPreference(key = SettingsRepository.BooleanPreferenceType.AUTO_SCAN_FILES)
        .collectAsState(initial = false)
    val selectedFrequency by viewModel.getPreference(key = SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ)
        .collectAsState(initial = 0)

    val context = LocalContext.current
    var isDirectoryPicked by rememberSaveable { mutableStateOf(false) }
    var showRemovalDialog by remember {
        mutableStateOf(false)
    }
    val totalDocuments by viewModel.totalDocuments.collectAsState(initial = 0)
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
                title = { Text(text = stringResource(LLString.fileScan)) },
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
                                    overlineContent = { Text(text = stringResource(LLString.scanning)) },
                                    headlineContent = {
                                        Card {
                                            SwitchSettingItem(
                                                label = stringResource(LLString.autoStartScans),
                                                description = stringResource(LLString.autoStartScansDesc),
                                                checked = autoStartScans,
                                                onCheckChanged = {
                                                    viewModel.switchPreference(
                                                        key = SettingsRepository.BooleanPreferenceType.AUTO_SCAN_FILES,
                                                        newValue = it
                                                    )

                                                    if (!it) {
                                                        viewModel.cancelPeriodicScan()
                                                    }
                                                }
                                            )

                                            if (autoStartScans) {
                                                val values = mapOf(
                                                    6 to stringResource(LLString.every6Hours),
                                                    12 to stringResource(LLString.every12Hours),
                                                    24 to stringResource(LLString.daily),
                                                    48 to stringResource(LLString.every2Days),
                                                    168 to stringResource(LLString.weekly)
                                                )

                                                DialogSettingItem(
                                                    title = stringResource(LLString.fileScanFrequency),
                                                    values = values.values.toList(),
                                                    selected = values[selectedFrequency]
                                                        ?: stringResource(LLString.off),
                                                    tonalElevation = tonalElevation,
                                                    onSelectionChange = { newValue ->
                                                        when (newValue) {
                                                            context.getString(LLString.every6Hours) -> {
                                                                viewModel.switchPreference(
                                                                    SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ,
                                                                    6
                                                                )
                                                            }

                                                            context.getString(LLString.every12Hours) -> {
                                                                viewModel.switchPreference(
                                                                    SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ,
                                                                    12
                                                                )
                                                            }

                                                            context.getString(LLString.daily) -> {
                                                                viewModel.switchPreference(
                                                                    SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ,
                                                                    24
                                                                )
                                                            }

                                                            context.getString(LLString.every2Days) -> {
                                                                viewModel.switchPreference(
                                                                    SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ,
                                                                    48
                                                                )
                                                            }

                                                            context.getString(LLString.weekly) -> {
                                                                viewModel.switchPreference(
                                                                    SettingsRepository.IntPreferenceType.AUTO_FILE_SCAN_FREQ,
                                                                    168
                                                                )
                                                            }
                                                        }


                                                        viewModel.togglePeriodicScans(
                                                            selectedFrequency
                                                        )
                                                    }
                                                )
                                            }

                                            ListItem(
                                                headlineContent = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        content = {
                                                            Text(text = stringResource(LLString.totalDocuments))
                                                            Text(text = totalDocuments.toString())
                                                        }
                                                    )
                                                },
                                                supportingContent = {
                                                    Text(stringResource(LLString.totalDocumentsDesc))
                                                },
                                                tonalElevation = tonalElevation,
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = stringResource(LLString.startScan))
                                                },
                                                supportingContent = {
                                                    Text(stringResource(LLString.startScanDesc))
                                                },
                                                trailingContent = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Search,
                                                        contentDescription = stringResource(LLString.startScan)
                                                    )
                                                },
                                                tonalElevation = tonalElevation,
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
                                                    Text(text = stringResource(LLString.reloadCoversLabel))
                                                },
                                                supportingContent = {
                                                    Text(text = stringResource(LLString.reloadCoversDesc))
                                                },
                                                trailingContent = {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Image,
                                                        contentDescription = stringResource(LLString.reloadCoversLabel)
                                                    )
                                                },
                                                tonalElevation = tonalElevation,
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
                                    overlineContent = { Text(text = stringResource(LLString.scanLocations)) },
                                    headlineContent = {
                                        Card {
                                            ListItem(
                                                headlineContent = {
                                                    Text(text = stringResource(LLString.selectAppDir))
                                                },
                                                supportingContent = {
                                                    if (isDirectoryPicked) {
                                                        Text(
                                                            text = stringResource(
                                                                LLString.currentAppDir,
                                                                viewModel.documentDirectory?.path.toString()
                                                            )
                                                        )
                                                    } else {
                                                        Text(text = stringResource(LLString.norDirMessage))
                                                    }
                                                },
                                                tonalElevation = tonalElevation,
                                                modifier = Modifier.clickable {
                                                    directoryPickerLauncher.launch(intent)
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = stringResource(LLString.selectOtherDirs))
                                                },
                                                supportingContent = {
                                                    Text(
                                                        text = stringResource(
                                                            LLString.totalSelected,
                                                            viewModel.scanDirectories.size
                                                        )
                                                    )
                                                },
                                                tonalElevation = tonalElevation,
                                                modifier = Modifier.clickable {
                                                    otherDirectoryPickerLauncher.launch(intent)
                                                }
                                            )

                                            ListItem(
                                                headlineContent = {
                                                    Text(text = stringResource(LLString.removeOtherDirs))
                                                },
                                                supportingContent = {
                                                    Text(stringResource(LLString.removeOtherDirsDesc))
                                                },
                                                tonalElevation = tonalElevation,
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
                        content = { Text(text = stringResource(LLString.cancel)) },
                        onClick = { showRemovalDialog = false }
                    )
                },
                title = {
                    Text(stringResource(LLString.scannedDirs))
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
                                                        contentDescription = stringResource(LLString.delete)
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