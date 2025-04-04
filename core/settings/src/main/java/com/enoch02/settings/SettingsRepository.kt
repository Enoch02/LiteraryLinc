package com.enoch02.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    suspend fun switchPreference(preference: BooleanPreferenceType, newValue: Boolean) {
        val key = getKeyForPreference(preference)

        context.dataStore.edit { settings ->
            settings[key] = newValue
        }
    }

    fun getPreference(preference: BooleanPreferenceType): Flow<Boolean> {
        val key = getKeyForPreference(preference)
        val flow: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[key] ?: false
            }
        return flow
    }

    suspend fun switchPreference(preference: IntPreferenceType, newValue: Int) {
        val key = getKeyForPreference(preference)

        context.dataStore.edit { settings ->
            settings[key] = newValue
        }
    }

    fun getPreference(preference: IntPreferenceType): Flow<Int> {
        val key = getKeyForPreference(preference)
        val flow: Flow<Int> =
            context.dataStore.data.map { preferences ->
                preferences[key] ?: 0
            }
        return flow
    }

    suspend fun switchPreference(preference: LongPreferenceType, newValue: Long) {
        val key = getKeyForPreference(preference)

        context.dataStore.edit { settings ->
            settings[key] = newValue
        }
    }

    fun getPreference(preference: LongPreferenceType): Flow<Long> {
        val key = getKeyForPreference(preference)
        val flow: Flow<Long> =
            context.dataStore.data.map { preferences ->
                preferences[key] ?: 0
            }
        return flow
    }

    // Private keys to restrict access
    private object Keys {
        val darkModeKey = booleanPreferencesKey("dark_mode")
        val dynamicColorKey = booleanPreferencesKey("dynamic_color")
        val volumeButtonPagingKey = booleanPreferencesKey("volume_btn_paging")
        val autoScanFilesKey = booleanPreferencesKey("auto_scan_files")
        val showDocViewerBars = booleanPreferencesKey("show_doc_viewer_bars")

        val currentReaderFilterKey = intPreferencesKey("current_reader_filter")
        val autoFileScanFrequency = intPreferencesKey("auto_file_scan_duration")
        val currentReadingStreak = intPreferencesKey("current_reading_streak")
        val longestReadingStreak = intPreferencesKey("longest_reading_streak")
        val bookReadingTarget = intPreferencesKey("book_reading_target")
        val bookReadingProgress = intPreferencesKey("book_reading_progress")

        val lastBookOpenedTimestamp = longPreferencesKey("last_book_opened_timestamp")
        val lastStreakUpdateTimestamp = longPreferencesKey("last_streak_update_timestamp")
    }

    // Map enum to preference keys
    private fun getKeyForPreference(preference: BooleanPreferenceType): Preferences.Key<Boolean> {
        return when (preference) {
            BooleanPreferenceType.DARK_MODE -> Keys.darkModeKey
            BooleanPreferenceType.DYNAMIC_COLOR -> Keys.dynamicColorKey
            BooleanPreferenceType.VOLUME_BTN_PAGING -> Keys.volumeButtonPagingKey
            BooleanPreferenceType.AUTO_SCAN_FILES -> Keys.autoScanFilesKey
            BooleanPreferenceType.SHOW_DOC_VIEWER_BARS -> Keys.showDocViewerBars
        }
    }

    private fun getKeyForPreference(preference: IntPreferenceType): Preferences.Key<Int> {
        return when (preference) {
            IntPreferenceType.CURRENT_READER_FILTER -> Keys.currentReaderFilterKey
            IntPreferenceType.AUTO_FILE_SCAN_FREQ -> Keys.autoFileScanFrequency
            IntPreferenceType.CURRENT_READING_STREAK -> Keys.currentReadingStreak
            IntPreferenceType.LONGEST_READING_STREAK -> Keys.longestReadingStreak
            IntPreferenceType.BOOK_READING_TARGET -> Keys.bookReadingTarget
            IntPreferenceType.BOOK_READING_PROGRESS -> Keys.bookReadingProgress
        }
    }

    private fun getKeyForPreference(preference: LongPreferenceType): Preferences.Key<Long> {
        return when (preference) {
            LongPreferenceType.LAST_BOOK_OPENED_TIMESTAMP -> Keys.lastBookOpenedTimestamp
            LongPreferenceType.LAST_STREAK_UPDATE_TIMESTAMP -> Keys.lastStreakUpdateTimestamp
        }
    }

    enum class BooleanPreferenceType {
        DARK_MODE,
        DYNAMIC_COLOR,
        VOLUME_BTN_PAGING,
        AUTO_SCAN_FILES,
        SHOW_DOC_VIEWER_BARS
    }

    enum class IntPreferenceType {
        CURRENT_READER_FILTER,
        AUTO_FILE_SCAN_FREQ,
        CURRENT_READING_STREAK,
        LONGEST_READING_STREAK,
        BOOK_READING_TARGET,
        BOOK_READING_PROGRESS
    }

    enum class LongPreferenceType {
        LAST_BOOK_OPENED_TIMESTAMP,
        LAST_STREAK_UPDATE_TIMESTAMP
    }
}