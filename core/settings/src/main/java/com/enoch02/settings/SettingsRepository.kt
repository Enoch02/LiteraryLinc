package com.enoch02.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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

    suspend fun switchPreference(preference: FloatPreferenceType, newValue: Float) {
        val key = getKeyForPreference(preference)

        context.dataStore.edit { settings ->
            settings[key] = newValue
        }
    }

    fun getPreference(preference: FloatPreferenceType): Flow<Float> {
        val key = getKeyForPreference(preference)
        val flow: Flow<Float> =
            context.dataStore.data.map { preferences ->
                preferences[key] ?: 0f
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

    // Private keys to restrict access
    private object Keys {
        val darkModeKey = booleanPreferencesKey("dark_mode")
        val dynamicColorKey = booleanPreferencesKey("dynamic_color")
        val confirmDialogKey = booleanPreferencesKey("confirm_dialogs")
        val volumeButtonPagingKey = booleanPreferencesKey("volume_btn_paging")
        val documentScaleKey = floatPreferencesKey("document_scale")
        val currentReaderFilterKey = intPreferencesKey("current_reader_filter")
        val pageRenderMethodKey = intPreferencesKey("render_method")
    }

    // Map enum to preference keys
    private fun getKeyForPreference(preference: BooleanPreferenceType): Preferences.Key<Boolean> {
        return when (preference) {
            BooleanPreferenceType.DARK_MODE -> Keys.darkModeKey
            BooleanPreferenceType.DYNAMIC_COLOR -> Keys.dynamicColorKey
            BooleanPreferenceType.CONFIRM_DIALOGS -> Keys.confirmDialogKey
            BooleanPreferenceType.VOLUME_BTN_PAGING -> Keys.volumeButtonPagingKey
        }
    }

    private fun getKeyForPreference(preference: FloatPreferenceType): Preferences.Key<Float> {
        return when (preference) {
            FloatPreferenceType.DOC_PAGE_SCALE -> Keys.documentScaleKey
        }
    }

    private fun getKeyForPreference(preference: IntPreferenceType): Preferences.Key<Int> {
        return when (preference) {
            IntPreferenceType.CURRENT_READER_FILTER -> Keys.currentReaderFilterKey
            IntPreferenceType.PAGE_RENDER_METHOD -> Keys.pageRenderMethodKey
        }
    }

    enum class BooleanPreferenceType {
        DARK_MODE,
        DYNAMIC_COLOR,
        CONFIRM_DIALOGS,
        VOLUME_BTN_PAGING
    }

    enum class FloatPreferenceType {
        DOC_PAGE_SCALE
    }

    enum class IntPreferenceType {
        CURRENT_READER_FILTER,
        PAGE_RENDER_METHOD
    }
}