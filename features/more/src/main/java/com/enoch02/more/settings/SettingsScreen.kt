package com.enoch02.more.settings

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.more.R
import com.enoch02.setting.SettingViewModel

//TODO: add option to refresh covers that are FROM open library api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingViewModel = hiltViewModel()) {
    val alwaysDark by viewModel.getBooleanPreference(key = viewModel.darkModeKey)
        .collectAsState(initial = false)
    val dynamicColors by viewModel.getBooleanPreference(key = viewModel.dynamicColorKey)
        .collectAsState(initial = false)
    val disableAnim by viewModel.getBooleanPreference(key = viewModel.animationKey)
        .collectAsState(initial = false)
    val showConfirmDialog by viewModel.getBooleanPreference(key = viewModel.confirmDialogKey)
        .collectAsState(initial = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_text)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = null/*TODO*/
                            )
                        }
                    )
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                content = {
                    item {
                        ListItem(
                            overlineContent = { Text(text = "Appearance") },
                            headlineContent = {
                                SwitchSettingItem(
                                    label = "Always dark mode",
                                    description = "Dark mode is always on",
                                    checked = alwaysDark,
                                    onCheckChanged = {
                                        viewModel.switchBooleanPreference(
                                            key = viewModel.darkModeKey,
                                            newValue = it
                                        )
                                    }
                                )

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    SwitchSettingItem(
                                        label = "Dynamic Colors",
                                        description = "Toggle dynamic colors",
                                        checked = dynamicColors,
                                        onCheckChanged = {
                                            viewModel.switchBooleanPreference(
                                                key = viewModel.dynamicColorKey,
                                                newValue = it
                                            )
                                        }
                                    )

                                }

                                SwitchSettingItem(
                                    label = "Disable Animations",
                                    checked = disableAnim,
                                    onCheckChanged = {
                                        viewModel.switchBooleanPreference(
                                            key = viewModel.animationKey,
                                            newValue = it
                                        )
                                    }
                                )
                            },
                        )
                    }

                    item {
                        ListItem(
                            overlineContent = { Text(text = "Behaviour") },
                            headlineContent = {
                                SwitchSettingItem(
                                    label = "Show confirmation before deletion",
                                    checked = showConfirmDialog,
                                    onCheckChanged = {
                                        viewModel.switchBooleanPreference(
                                            key = viewModel.confirmDialogKey,
                                            newValue = it
                                        )
                                    }
                                )
                            }
                        )
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    )
}