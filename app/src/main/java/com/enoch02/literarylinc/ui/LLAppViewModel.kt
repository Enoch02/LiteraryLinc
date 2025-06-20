package com.enoch02.literarylinc.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enoch02.database.model.ReaderFilter
import com.enoch02.database.model.StatusFilter
import com.enoch02.settings.ReadingProgressManager
import com.enoch02.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LLAppViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val readingProgressManager: ReadingProgressManager
) :
    ViewModel() {
    val readingGoal = readingProgressManager.getReadingGoal()
    val readingProgress = readingProgressManager.getReadingGoalProgress()

    private fun changeIntPreference(key: SettingsRepository.IntPreferenceType, newValue: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.switchPreference(key, newValue)
        }
    }

    private fun getIntPreference(key: SettingsRepository.IntPreferenceType): Flow<Int> {
        return settingsRepository.getPreference(key)
    }

    fun getCurrentReaderFilter(): Flow<ReaderFilter> {
        val ordinalFlow =
            getIntPreference(SettingsRepository.IntPreferenceType.CURRENT_READER_FILTER)

        return ordinalFlow.map {
            val filter: ReaderFilter? = fromOrdinal<ReaderFilter>(it)
            filter ?: ReaderFilter.ALL
        }
    }

    fun changeReaderFilter(newValue: ReaderFilter) {
        changeIntPreference(
            SettingsRepository.IntPreferenceType.CURRENT_READER_FILTER,
            newValue.ordinal
        )
    }

    fun getCurrentBookStatusFilter(): Flow<StatusFilter> {
        val ordinalFlow =
            getIntPreference(SettingsRepository.IntPreferenceType.CURRENT_BOOKLIST_STATUS_FILTER)

        return ordinalFlow.map {
            val filter: StatusFilter? = fromOrdinal<StatusFilter>(it)
            filter ?: StatusFilter.ALL
        }
    }

    fun changeBookStatusFilter(newValue: StatusFilter) {
        changeIntPreference(
            SettingsRepository.IntPreferenceType.CURRENT_BOOKLIST_STATUS_FILTER,
            newValue.ordinal
        )
    }

    private inline fun <reified T : Enum<T>> fromOrdinal(ordinal: Int): T? {
        return enumValues<T>().getOrNull(ordinal)
    }

    fun updateReadingGoalData(newGoal: Int, newProgress: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            readingProgressManager.setReadingGoal(newGoal)

            // only update if there is a change
            if (readingProgress.first() != newProgress) {
                readingProgressManager.updateReadingGoalProgress(newProgress)
            }
        }
    }
}