package com.enoch02.more.backup_restore

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.resources.LLString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavController,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val tonalElevation = 30.dp
    val createFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
    }
    val csvBackupLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                if (it.resultCode == RESULT_OK) {
                    viewModel.createCSVBackup(it.data?.data!!)
                }
            }
        )
    val excelFriendlyBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == RESULT_OK) {
                viewModel.createExcelFriendlyBackup(it.data?.data!!)
            }
        }
    )

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (it != null) {
                viewModel.restoreCSVBackup(it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(LLString.backupRestore)) },
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
        content = {
            LazyColumn(
                content = {
                    item {
                        ListItem(
                            overlineContent = { Text(text = "CSV backup") },
                            headlineContent = {
                                Card(
                                    content = {
                                        ListItem(
                                            headlineContent = { Text(text = stringResource(LLString.createCSV)) },
                                            supportingContent = { Text(text = stringResource(LLString.createCSVDesc)) },
                                            modifier = Modifier.clickable {
                                                csvBackupLauncher.launch(
                                                    createFileIntent.apply {
                                                        putExtra(
                                                            Intent.EXTRA_TITLE,
                                                            "literarylinc_csv_backup-${
                                                                formatEpochAsString(
                                                                    System.currentTimeMillis()
                                                                )
                                                            }"
                                                        )
                                                    }
                                                )
                                            },
                                            tonalElevation = tonalElevation
                                        )

                                        HorizontalDivider()

                                        ListItem(
                                            headlineContent = { Text(text = stringResource(LLString.restoreCSV)) },
                                            supportingContent = { Text(text = stringResource(LLString.restoreCSVDesc)) },
                                            modifier = Modifier.clickable {
                                                openFileLauncher.launch("*/*")
                                            },
                                            tonalElevation = tonalElevation
                                        )

                                        HorizontalDivider()

                                        ListItem(
                                            headlineContent = { Text(text = stringResource(LLString.excelFriendlyExport)) },
                                            supportingContent = { Text(text = stringResource(LLString.excelFriendlyExportDesc)) },
                                            modifier = Modifier.clickable {
                                                excelFriendlyBackupLauncher.launch(
                                                    createFileIntent.apply {
                                                        putExtra(
                                                            Intent.EXTRA_TITLE,
                                                            "literarylinc_friendly_csv_backup-${
                                                                formatEpochAsString(
                                                                    System.currentTimeMillis()
                                                                )
                                                            }"
                                                        )
                                                    }
                                                )
                                            },
                                            tonalElevation = tonalElevation
                                        )
                                    }
                                )
                            }
                        )
                    }
                },
                modifier = Modifier.padding(it)
            )
        }
    )
}