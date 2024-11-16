package com.enoch02.more.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.resources.BitmapManager
import com.enoch02.resources.cleanup
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bitmapManager: BitmapManager
) :
    ViewModel() {
    fun switchPreference(key: SettingsRepository.BooleanPreferenceType, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: SettingsRepository.BooleanPreferenceType): Flow<Boolean> {
        return settingsRepository.getPreference(key)
    }

    fun switchPreference(key: SettingsRepository.FloatPreferenceType, newValue: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    fun getPreference(key: SettingsRepository.FloatPreferenceType): Flow<Float> {
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

    fun cleanUpOldBitmaps() {
        viewModelScope.launch(Dispatchers.IO) {
            bitmapManager.cleanup()
        }
    }
}