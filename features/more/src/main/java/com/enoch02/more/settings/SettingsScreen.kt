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
import com.enoch02.more.settings.components.RenderMethodSelector
import com.enoch02.more.settings.components.ScaleSelector
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
    val documentScale by viewModel.getPreference(SettingsRepository.FloatPreferenceType.DOC_PAGE_SCALE)
        .collectAsState(initial = 0f)
    val renderMethod by viewModel.getPreference(SettingsRepository.IntPreferenceType.PAGE_RENDER_METHOD)
        .collectAsState(initial = 0)

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

                                        ScaleSelector(selectedScale = documentScale) { scale ->
                                            viewModel.switchPreference(
                                                SettingsRepository.FloatPreferenceType.DOC_PAGE_SCALE,
                                                scale
                                            )
                                        }

                                        RenderMethodSelector(selectedMethod = renderMethod) { method ->
                                            viewModel.switchPreference(
                                                SettingsRepository.IntPreferenceType.PAGE_RENDER_METHOD,
                                                method
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
                                        SwitchSettingItem(
                                            label = "Show confirmation before deletion",
                                            checked = showConfirmDialog,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.CONFIRM_DIALOGS,
                                                    newValue = it
                                                )
                                            }
                                        )

                                        SwitchSettingItem(
                                            label = "Use volume buttons to change pages",
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