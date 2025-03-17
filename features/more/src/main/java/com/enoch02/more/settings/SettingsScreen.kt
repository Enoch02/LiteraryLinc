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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.enoch02.more.R
import com.enoch02.more.settings.components.ConfirmationSettingItem
import com.enoch02.more.settings.components.SwitchSettingItem
import com.enoch02.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val alwaysDark by viewModel.alwaysDark.collectAsState(false)
    val dynamicColors by viewModel.dynamicColors.collectAsState(false)
    val volumeButtonPaging by viewModel.volumeButtonPaging.collectAsState(false)
    val showDocViewerBars by viewModel.showDocViewerBars.collectAsState(false)

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
                                Text(text = stringResource(R.string.behaviour))
                            },
                            headlineContent = {
                                Card(
                                    content = {
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

                                        SwitchSettingItem(
                                            label = stringResource(R.string.show_doc_viewer_bars),
                                            description = stringResource(R.string.show_doc_viewer_bars_desc),
                                            checked = showDocViewerBars,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.SHOW_DOC_VIEWER_BARS,
                                                    newValue = it
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }

                    item {
                        ListItem(
                            overlineContent = {
                                Text(text = stringResource(R.string.data))
                            },
                            headlineContent = {
                                Card(
                                    content = {
                                        ConfirmationSettingItem(
                                            label = stringResource(R.string.reset_booklist),
                                            onClick = { viewModel.resetBooklist(context) },
                                            description = stringResource(R.string.reset_booklist_desc),
                                            alertMsg = stringResource(R.string.reset_booklist_warning_msg)
                                        )

                                        ConfirmationSettingItem(
                                            label = stringResource(R.string.reset_readerlist),
                                            onClick = { viewModel.resetReaderList(context) },
                                            description = stringResource(R.string.reset_readerlist_desc),
                                            alertMsg = stringResource(R.string.reset_readerlist_warning_msg)
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