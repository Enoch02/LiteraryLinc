package com.enoch02.more.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val settingsRepository: SettingsRepository) :
    ViewModel() {
    fun switchBooleanPreference(key: SettingsRepository.BooleanPreferenceType, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchBooleanPreference(key, newValue)
        }
    }

    fun getBooleanPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getBooleanPreference(key)
    }

    fun changeFloatPreference(key: SettingsRepository.FloatPreferenceType, newValue: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.changeFloatPreference(key, newValue)
        }
    }

    fun getFloatPreference(key: SettingsRepository.FloatPreferenceType): Flow<Float> {
        return settingsRepository.getFloatPreference(key)
    }
}