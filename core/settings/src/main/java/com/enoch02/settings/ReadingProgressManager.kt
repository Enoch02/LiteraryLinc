package com.enoch02.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ReadingProgressManager(val settingsRepository: SettingsRepository) {

    fun getReadingStreak(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK)
    }

    fun getLongestReadingStreak(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK)
    }

    suspend fun updateReadingStreak() {
        val lastOpened = settingsRepository
            .getPreference(SettingsRepository.LongPreferenceType.LAST_BOOK_OPENED_TIMESTAMP)
            .first()
        val currentTime = System.currentTimeMillis()

        if (isDifferenceGreaterThanOneDay(lastOpened, currentTime)) {
            settingsRepository
                .switchPreference(SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK, 0)
        } else {
            val current = getReadingStreak().first()

            settingsRepository
                .switchPreference(
                    SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                    current + 1
                )
        }

        // update the date a book was last opened
        settingsRepository.switchPreference(
            SettingsRepository.LongPreferenceType.LAST_BOOK_OPENED_TIMESTAMP,
            System.currentTimeMillis()
        )
        checkForNewLongestStreak()
    }

    private fun isDifferenceGreaterThanOneDay(timestamp1: Long, timestamp2: Long): Boolean {
        val differenceInMillis = abs(timestamp1 - timestamp2)
        val onDayInMillis = TimeUnit.DAYS.toMillis(1)

        return differenceInMillis > onDayInMillis
    }

    private suspend fun checkForNewLongestStreak() {
        val current = getReadingStreak().first()
        val storedLongestStreak = settingsRepository
            .getPreference(SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK)
            .first()

        if (current > storedLongestStreak) {
            settingsRepository
                .switchPreference(
                    SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK,
                    current
                )
        }
    }
}