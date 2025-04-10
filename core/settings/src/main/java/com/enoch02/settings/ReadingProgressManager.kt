package com.enoch02.settings

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

private const val TAG = "ReadingProgressManager"

class ReadingProgressManager(private val settingsRepository: SettingsRepository) {
    private val oneDayInMillis = 24 * 60 * 60 * 1000L // 24 hours in milliseconds

    //    private val oneAndHalfDays = (oneDayInMillis * 1.5).toLong()
    private val oneDayAndSixHoursInMillis = (oneDayInMillis * 1.25).toLong()


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
     */
    suspend fun updateReadingStreak() {
        val lastStreakUpdateTime = settingsRepository
            .getPreference(SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP)
            .first()
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "last streak update time = $lastStreakUpdateTime.")
        Log.d(TAG, "current time = $currentTime.")

        // calculate days difference using milliseconds
        val timeDifference = currentTime - lastStreakUpdateTime

        when {
            timeDifference > oneDayAndSixHoursInMillis -> {
                resetReadingStreak()
            }

            timeDifference >= oneDayInMillis -> {
                // between 24-36 hours (1-1.25 days)
                Log.i(TAG, "updateReadingStreak: Exactly one day passed. Incrementing streak.")
                val newStreak = getReadingStreak().first() + 1

                settingsRepository
                    .switchPreference(
                        SettingsRepository.IntPreferenceType.CURRENT_READING_STREAK,
                        newStreak
                    )

                settingsRepository.switchPreference(
                    SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP,
                    currentTime
                )
            }

            else -> {
                // less than 24 hours - no streak change
                Log.i(TAG, "updateReadingStreak: Less than one day passed. No streak change.")
            }
        }

        checkForNewLongestStreak()
        checkForTimeManipulation()
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

    /**
     * Returns the timestamp when the current streak will expire.
     * This represents the deadline before which the user needs to read again.
     */
    private suspend fun getStreakExpirationTimestamp(): Long {
        val lastOpened = settingsRepository
            .getPreference(SettingsRepository.LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP)
            .first()

        // Calculate deadline timestamp
        return lastOpened + oneDayAndSixHoursInMillis
    }

    /**
     * Returns the time remaining before the streak ends, in milliseconds.
     * Negative value indicates the streak has already expired.
     */
    suspend fun getTimeRemainingForStreak(): Long {
        val currentTime = System.currentTimeMillis()
        val deadlineTimestamp = getStreakExpirationTimestamp()

        return deadlineTimestamp - currentTime
    }

    private suspend fun resetReadingStreak() {
        Log.i(TAG, "resetReadingStreak: More than one day passed. Streak reset.")

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

    /**
     * checks if date was moved to a future (or past) date and time then reverted to the current time
     */
    suspend fun checkForTimeManipulation() {
        val remainingMillis = getTimeRemainingForStreak()
        Log.d(TAG, "checkForTimeManipulation: remainingTimeForStreakInMillis=$remainingMillis")

        if (remainingMillis >= oneDayAndSixHoursInMillis || remainingMillis < 0) {
            Log.i(TAG, "checkForTimeManipulation: time manipulation detected! resetting streak")
            resetReadingStreak()
        }
    }
}