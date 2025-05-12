package com.enoch02.settings

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val TAG = "ReadingProgressManager"

class ReadingProgressManager(private val settingsRepository: SettingsRepository) {
    /**
     * Obtain the reading streak stored in the repository
     */
    fun getReadingStreak(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK)
    }

    /**
     * Obtain the longest reading streak stored in the repository
     * */
    fun getLongestReadingStreak(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.LONGEST_READING_STREAK)
    }

    /**
     * Updates the stored reading streak. Resets the value to 0 if over
     * a day has passed, else it increments the streak by 1.
     *
     * @param isBookOpen was this called after opening a book?
     */
    suspend fun updateReadingStreak(isBookOpen: Boolean) {
        val lastStreakUpdateTime = settingsRepository
            .getPreference(SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP)
            .first()
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "last streak update time = ${formatMillisAsString(lastStreakUpdateTime)}.")
        Log.d(TAG, "current time = ${formatMillisAsString(currentTime)}")

        val lastCal =
            Calendar.getInstance().apply { timeInMillis = lastStreakUpdateTime ?: currentTime }
        val currentCal = Calendar.getInstance().apply { timeInMillis = currentTime }

        val lastYear = lastCal.get(Calendar.YEAR)
        val lastDayOfYear = lastCal.get(Calendar.DAY_OF_YEAR)

        val currentYear = currentCal.get(Calendar.YEAR)
        val currentDayOfYear = currentCal.get(Calendar.DAY_OF_YEAR)

        val isSameDay = lastYear == currentYear && lastDayOfYear == currentDayOfYear
        val daysBetween = if (lastYear == currentYear) {
            currentDayOfYear - lastDayOfYear
        } else {
            // Handle year change (e.g., Dec 31 -> Jan 1)
            val daysInLastYear = lastCal.getActualMaximum(Calendar.DAY_OF_YEAR)
            (daysInLastYear - lastDayOfYear) + currentDayOfYear
        }

        when {
            isSameDay -> {
                Log.i(TAG, "updateReadingStreak: Same calendar day. No streak change.")
            }

            daysBetween == 1 -> {
                if (isBookOpen) {
                    Log.i(TAG, "updateReadingStreak: New calendar day. Incrementing streak.")
                    val newStreak = getReadingStreak().first() + 1

                    settingsRepository.switchPreference(
                        SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                        newStreak
                    )
                }
            }

            daysBetween < 0 -> {
                resetReadingStreak("Possible time manipulation detected. Streak reset.")
            }

            else -> {
                resetReadingStreak("More than one day passed. Streak reset.")
            }
        }

        settingsRepository.switchPreference(
            SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP,
            currentTime
        )
        checkForNewLongestStreak()
    }


    /**
     * Compares the current streak with the stored longest streak.
     * Updates the longest streak value if current > storedLongest.
     */
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

    private suspend fun resetReadingStreak(message: String) {
        Log.i(TAG, "resetReadingStreak: $message")

        settingsRepository
            .switchPreference(
                SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                0
            )

        // update the last time the streak was updated
        settingsRepository.switchPreference(
            SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP,
            System.currentTimeMillis()
        )
    }

    /**
     * Save reading goal into shared prefs.
     *
     * @param target desired value
     */
    suspend fun setReadingGoal(target: Int) {
        settingsRepository
            .switchPreference(SettingsRepository.IntPreferenceType.BOOK_READING_TARGET, target)
    }

    /**
     * Used to manually set the reading progress
     *
     * @param newValue desired value
     */
    suspend fun updateReadingGoalProgress(newValue: Int) {
        settingsRepository.switchPreference(
            SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS,
            newValue
        )
    }

    /**
     * Used to update reading goal progress whenever a book has been completed.
     * Increments the stored value by 1.
     */
    suspend fun incrementReadingGoalProgress() {
        settingsRepository.switchPreference(
            SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS,
            getReadingGoalProgress().first() + 1
        )
    }

    /**
     * Obtain the reading goal stored in the repository
     */
    fun getReadingGoal(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.BOOK_READING_TARGET)
    }

    /**
     * Obtain the reading goal progress stored in the repository
     */
    fun getReadingGoalProgress(): Flow<Int> {
        return settingsRepository.getPreference(SettingsRepository.IntPreferenceType.BOOK_READING_PROGRESS)
    }

    private fun formatMillisAsString(date: Long?): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && date != null -> {
                val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm")
                val dateObj = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(date),
                    ZoneId.systemDefault()
                )
                formatter.format(dateObj)
            }

            Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && date != null -> {
                val formatter = SimpleDateFormat("dd MMM yyyy hh:mm", Locale.ROOT)
                formatter.format(Date(date))
            }

            else -> {
                ""
            }
        }
    }
}