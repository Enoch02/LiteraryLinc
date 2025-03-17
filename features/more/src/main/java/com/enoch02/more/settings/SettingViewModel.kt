package com.enoch02.more.settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.dao.BookDao
import com.enoch02.database.dao.DocumentDao
import com.enoch02.settings.SettingsRepository
import com.enoch02.settings.SettingsRepository.BooleanPreferenceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val bookDao: BookDao,
    private val documentDao: DocumentDao
) :
    ViewModel() {
    val alwaysDark = getPreference(key = BooleanPreferenceType.DARK_MODE)
    val dynamicColors = getPreference(key = BooleanPreferenceType.DYNAMIC_COLOR)
    val volumeButtonPaging = getPreference(key = BooleanPreferenceType.VOLUME_BTN_PAGING)
    val showDocViewerBars = getPreference(key = BooleanPreferenceType.SHOW_DOC_VIEWER_BARS)

    fun switchPreference(key: BooleanPreferenceType, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    private fun getPreference(key: BooleanPreferenceType): Flow<Boolean> {
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

    fun resetBooklist(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            bookDao.deleteAll()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Booklist cleared!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resetReaderList(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            documentDao.deleteAll()

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Readerlist cleared!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}