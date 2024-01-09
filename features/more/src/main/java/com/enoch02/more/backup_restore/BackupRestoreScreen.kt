package com.enoch02.more.backup_restore

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.database.util.formatEpochDate
import com.enoch02.more.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    navController: NavController,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val openFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/csv"
        putExtra(
            Intent.EXTRA_TITLE,
            "literarylinc_csv_backup-${formatEpochDate(System.currentTimeMillis())}"
        )
    }
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                viewModel.createBackup(it.data?.data!!)
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.backup_and_restore)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
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
                            headlineContent = { Text(text = "Create CSV backup") },
                            supportingContent = { Text(text = "Backup entries in a CSV file. Cover Images are not backed up") },
                            modifier = Modifier.clickable { launcher.launch(openFileIntent) }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text(text = "Restore CSV backup") },
                            supportingContent = { Text(text = "Restore backup from a CSV file") },
                            modifier = Modifier.clickable { }
                        )
                    }
                    item { Divider() }

                    item {
                        ListItem(
                            overlineContent = { Text(text = "Automatic backups") },
                            headlineContent = { Text(text = "") },
                            supportingContent = { Text(text = "") }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text(text = "") },
                            supportingContent = { Text(text = "") }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text(text = "") },
                            supportingContent = { Text(text = "") }
                        )
                    }
                },
                modifier = Modifier.padding(it)
            )
        }
    )
}