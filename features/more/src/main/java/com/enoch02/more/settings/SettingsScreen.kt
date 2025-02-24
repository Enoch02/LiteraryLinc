package com.enoch02.more.settings

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
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
import com.enoch02.more.settings.components.SwitchSettingItem
import com.enoch02.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingViewModel = hiltViewModel()) {
    val alwaysDark by viewModel.getPreference(key = SettingsRepository.BooleanPreferenceType.DARK_MODE)
        .collectAsState(initial = false)
    val dynamicColors by viewModel.getPreference(key = SettingsRepository.BooleanPreferenceType.DYNAMIC_COLOR)
        .collectAsState(initial = false)
    val showConfirmDialog by viewModel.getPreference(key = SettingsRepository.BooleanPreferenceType.CONFIRM_DIALOGS)
        .collectAsState(initial = false)
    val volumeButtonPaging by viewModel.getPreference(key = SettingsRepository.BooleanPreferenceType.VOLUME_BTN_PAGING)
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
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(id = R.string.settings_text)
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
                            overlineContent = { Text(text = stringResource(R.string.appearance)) },
                            headlineContent = {
                                Card(
                                    content = {
                                        SwitchSettingItem(
                                            label = stringResource(R.string.always_dark_mode_label),
                                            description = stringResource(R.string.always_dark_mode_desc),
                                            checked = alwaysDark,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.DARK_MODE,
                                                    newValue = it
                                                )
                                            }
                                        )

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            SwitchSettingItem(
                                                label = stringResource(R.string.dynamic_colors_label),
                                                description = stringResource(R.string.toggle_dynamic_colors_desc),
                                                checked = dynamicColors,
                                                onCheckChanged = {
                                                    viewModel.switchPreference(
                                                        key = SettingsRepository.BooleanPreferenceType.DYNAMIC_COLOR,
                                                        newValue = it
                                                    )
                                                }
                                            )
                                        }
                                    }
                                )
                            },
                        )
                    }

                    item {
                        ListItem(
                            overlineContent = {
                                Text(text = "Behaviour")
                            },
                            headlineContent = {
                                Card(
                                    content = {
                                        //TODO: not yet implemented
                                        SwitchSettingItem(
                                            label = stringResource(R.string.show_confirmation_dialogs),
                                            description = stringResource(R.string.show_confirmation_dialogs_desc),
                                            checked = showConfirmDialog,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.CONFIRM_DIALOGS,
                                                    newValue = it
                                                )
                                            }
                                        )

                                        SwitchSettingItem(
                                            label = stringResource(R.string.volume_paging),
                                            description = stringResource(R.string.volume_paging_desc),
                                            checked = volumeButtonPaging,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.VOLUME_BTN_PAGING,
                                                    newValue = it
                                                )
                                            }
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