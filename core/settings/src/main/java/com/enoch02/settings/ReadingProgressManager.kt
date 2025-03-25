package com.enoch02.settings

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import kotlin.math.abs

private const val TAG = "ReadingProgressManager"

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

        Log.d(TAG, "last opened = $lastOpened.")
        Log.d(TAG, "current time = $currentTime.")

        // calculate days difference using milliseconds
        val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val timeDifference = currentTime - lastOpened

        when {
            timeDifference > oneDayInMillis * 1.5 -> {
                // more than 1.5 days (allowing for some buffer)
                Log.i(TAG, "updateReadingStreak: More than one day passed. Streak reset.")
                settingsRepository
                    .switchPreference(
                        SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                        0
                    )
            }

            timeDifference >= oneDayInMillis -> {
                // between 24-36 hours (1-1.5 days)
                Log.i(TAG, "updateReadingStreak: Exactly one day passed. Incrementing streak.")
                val current = getReadingStreak().first()
                settingsRepository
                    .switchPreference(
                        SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                        current + 1
                    )
            }

            else -> {
                // less than 24 hours - no streak change
                Log.i(TAG, "updateReadingStreak: Less than one day passed. No streak change.")
            }
        }

        // update last opened timestamp
        settingsRepository.switchPreference(
            SettingsRepository.LongPreferenceType.LAST_BOOK_OPENED_TIMESTAMP,
            currentTime
        )
        checkForNewLongestStreak()
    }

    private suspend fun checkForNewLongestStreak() {
        val current = getReadingStreak().first()
        val storedLongestStreak = settingsRepository
            .getPreference(SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK)
            .first()

        Log.d(TAG, "checkForNewLongestStreak: current=$current.")
        Log.d(TAG, "checkForNewLongestStreak: stored=$storedLongestStreak.")

        if (current > storedLongestStreak) {
            Log.i(TAG, "checkForNewLongestStreak: new longest streak set.")
            settingsRepository
                .switchPreference(
                    SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK,
                    current
                )
        }
    }

    suspend fun setReadingGoal(target: Int) {
        settingsRepository
            .switchPreference(SettingsRepository.IntPreferenceType.BOOK_READING_TARGET, target)
    }

    suspend fun updateReadingGoalProgress(newValue: Int) {
        settingsRepository.switchPreference(
            SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS,
            newValue
        )
    }

    suspend fun incrementReadingGoalProgress() {
        settingsRepository.switchPreference(
            SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS,
            getReadingGoalProgress().first() + 1
        )
    }

    fun getReadingGoal(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.BOOK_READING_TARGET)
    }

    fun getReadingGoalProgress(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS)
    }
}