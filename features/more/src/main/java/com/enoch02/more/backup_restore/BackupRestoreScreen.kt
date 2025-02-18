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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.database.util.formatEpochAsString
import com.enoch02.more.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavController,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val createFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(
            Intent.EXTRA_TITLE,
            "literarylinc_csv_backup-${formatEpochAsString(System.currentTimeMillis())}"
        )
    }
    val createFileLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                if (it.resultCode == RESULT_OK) {
                    viewModel.createCSVBackup(it.data?.data!!)
                }
            }
        )

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = {
            if (it != null) {
                viewModel.restoreCSVBackup(
                    it,
                    onSuccess = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                context.getString(R.string.restore_complete_success),
                                withDismissAction = true
                            )
                        }
                    })
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.backup_and_restore)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                        }
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                                            headlineContent = { Text(text = stringResource(R.string.create_csv_bkup)) },
                                            supportingContent = { Text(text = stringResource(R.string.create_csv_bkup_desc)) },
                                            modifier = Modifier.clickable {
                                                createFileLauncher.launch(
                                                    createFileIntent
                                                )
                                            },
                                            tonalElevation = 30.dp
                                        )

                                        HorizontalDivider()

                                        ListItem(
                                            headlineContent = { Text(text = stringResource(R.string.restore_csv_bkup)) },
                                            supportingContent = { Text(text = stringResource(R.string.restore_csv_bkup_desc)) },
                                            modifier = Modifier.clickable {
                                                openFileLauncher.launch("*/*")
                                            },
                                            tonalElevation = 30.dp
                                        )
                                    }
                                )
                            }
                        )
                    }

                    item {
                        // TODO
                        if (false) {
                            ListItem(
                                overlineContent = { Text(text = "Automatic backups") },
                                headlineContent = {
                                    Card {
                                        ListItem(
                                            headlineContent = { Text(text = "") },
                                            supportingContent = { Text(text = "") },
                                            tonalElevation = 30.dp
                                        )

                                        ListItem(
                                            headlineContent = { Text(text = "") },
                                            supportingContent = { Text(text = "") },
                                            tonalElevation = 30.dp
                                        )

                                        ListItem(
                                            headlineContent = { Text(text = "") },
                                            supportingContent = { Text(text = "") },
                                            tonalElevation = 30.dp
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.padding(it)
            )
        }
    )
}