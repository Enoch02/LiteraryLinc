package com.enoch02.more.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.settings.SettingsRepository
import com.enoch02.settings.SettingsRepository.BooleanPreferenceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) :
    ViewModel() {
    val alwaysDark = getPreference(key = BooleanPreferenceType.DARK_MODE)
    val dynamicColors = getPreference(key = BooleanPreferenceType.DYNAMIC_COLOR)
    val showConfirmDialog = getPreference(key = BooleanPreferenceType.CONFIRM_DIALOGS)
    val volumeButtonPaging = getPreference(key = BooleanPreferenceType.VOLUME_BTN_PAGING)
    val showDocViewerBars = getPreference(key = BooleanPreferenceType.SHOW_DOC_VIEWER_BARS)

    fun switchPreference(key: BooleanPreferenceType, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }

    fun switchPreference(key: SettingsRepository.IntPreferenceType, newValue: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: SettingsRepository.IntPreferenceType): Flow<Int> {
        return settingsRepository.getPreference(key)
    }
}