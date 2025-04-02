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
import com.enoch02.more.settings.components.ConfirmationSettingItem
import com.enoch02.more.settings.components.SwitchSettingItem
import com.enoch02.resources.LLString
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
                title = { Text(text = stringResource(id = LLString.settings)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(id = LLString.navigateBack)
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
                            overlineContent = { Text(text = stringResource(LLString.appearance)) },
                            headlineContent = {
                                Card(
                                    content = {
                                        SwitchSettingItem(
                                            label = stringResource(LLString.alwaysDarkLabel),
                                            description = stringResource(LLString.alwaysDarkDesc),
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
                                                label = stringResource(LLString.dynamicColorsLabel),
                                                description = stringResource(LLString.dynamicColorsDesc),
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
                                Text(text = stringResource(LLString.behavior))
                            },
                            headlineContent = {
                                Card(
                                    content = {
                                        SwitchSettingItem(
                                            label = stringResource(LLString.volumePaging),
                                            description = stringResource(LLString.volumePagingDesc),
                                            checked = volumeButtonPaging,
                                            onCheckChanged = {
                                                viewModel.switchPreference(
                                                    key = SettingsRepository.BooleanPreferenceType.VOLUME_BTN_PAGING,
                                                    newValue = it
                                                )
                                            }
                                        )

                                        SwitchSettingItem(
                                            label = stringResource(LLString.showDocViewerBars),
                                            description = stringResource(LLString.showDocViewerBarsDesc),
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
                                Text(text = stringResource(LLString.data))
                            },
                            headlineContent = {
                                Card(
                                    content = {
                                        ConfirmationSettingItem(
                                            label = stringResource(LLString.resetBooklist),
                                            onClick = { viewModel.resetBooklist(context) },
                                            description = stringResource(LLString.resetBooklistDesc),
                                            alertMsg = stringResource(LLString.resetBooklistWarning)
                                        )

                                        ConfirmationSettingItem(
                                            label = stringResource(LLString.resetReaderlist),
                                            onClick = { viewModel.resetReaderList(context) },
                                            description = stringResource(LLString.resetReaderlistDesc),
                                            alertMsg = stringResource(LLString.resetReaderlistWarning)
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