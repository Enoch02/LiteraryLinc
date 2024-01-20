package com.enoch02.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Divider
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
                        Icon(
                            painter = painterResource(id = R.drawable.round_timer_24),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.timer_text)) },
                    modifier = Modifier.clickable {
                        //TODO
                    }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_label_24),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.custom_tags_text)) },
                    modifier = Modifier.clickable {
                        //TODO
                    }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_star_24),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.wishlist_text)) },
                    modifier = Modifier.clickable {
                        //TODO
                    }
                )
            }

            item { Divider() }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.settings_text)) },
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
                    modifier = Modifier.clickable {
                        navController.navigate(MoreScreenDestination.BackupRestore.route)
                    }
                )
            }

            item {
                ListItem(
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_update_24),
                            contentDescription = null
                        )
                    },
                    headlineContent = { Text(stringResource(R.string.check_update_text)) },
                    modifier = Modifier.clickable {
                        //TODO
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
                    modifier = Modifier.clickable {
                        navController.navigate(MoreScreenDestination.About.route)
                    }
                )
            }
        },
        modifier = modifier
    )
}