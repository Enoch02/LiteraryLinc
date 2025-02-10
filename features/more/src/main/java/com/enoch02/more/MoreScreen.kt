package com.enoch02.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.enoch02.more.navigation.MoreScreenDestination


@Composable
fun MoreScreen(navController: NavController, modifier: Modifier) {
    LazyColumn(
        content = {
            item {
                ListItem(
                    leadingContent = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                    },
                    headlineContent = { Text(text = stringResource(R.string.file_scan_text)) },
                    supportingContent = {
                        Text(stringResource(R.string.file_scan_desc))
                    },
                    modifier = Modifier.clickable { navController.navigate(MoreScreenDestination.Scanner.route) }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.settings_text)) },
                    supportingContent = {
                        Text(stringResource(R.string.settings_desc))
                    },
                    modifier = Modifier.clickable { navController.navigate(MoreScreenDestination.Settings.route) }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_settings_backup_restore_24),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.bckup_res_text)) },
                    supportingContent = {
                        Text(stringResource(R.string.bckup_res_desc))
                    },
                    modifier = Modifier.clickable {
                        navController.navigate(MoreScreenDestination.BackupRestore.route)
                    }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.about_text)) },
                    supportingContent = {
                        Text(stringResource(R.string.about_desc))
                    },
                    modifier = Modifier.clickable {
                        navController.navigate(MoreScreenDestination.About.route)
                    }
                )
            }
        },
        modifier = modifier
    )
}