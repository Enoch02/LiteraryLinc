package com.enoch02.settings

import android.content.Context
import androidx.compose.ui.input.key.Key
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    suspend fun switchBooleanPreference(preference: PreferenceType, newValue: Boolean) {
        val key = getKeyForPreference(preference)

        context.dataStore.edit { settings ->
            settings[key] = newValue
        }
    }

    fun getBooleanPreference(preference: PreferenceType): Flow<Boolean> {
        val key = getKeyForPreference(preference)
        val flow: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[key] ?: false
            }
        return flow
    }

    // Private keys to restrict access
    private object Keys {
        val darkModeKey = booleanPreferencesKey("dark_mode")
        val dynamicColorKey = booleanPreferencesKey("dynamic_color")
        val confirmDialogKey = booleanPreferencesKey("confirm_dialogs")
        val volumeButtonPagingKey = booleanPreferencesKey("volume_btn_paging")
    }

    // Map enum to preference keys
    private fun getKeyForPreference(preference: PreferenceType): Preferences.Key<Boolean> {
        return when (preference) {
            PreferenceType.DARK_MODE -> Keys.darkModeKey
            PreferenceType.DYNAMIC_COLOR -> Keys.dynamicColorKey
            PreferenceType.CONFIRM_DIALOGS -> Keys.confirmDialogKey
            PreferenceType.VOLUME_BTN_PAGING -> Keys.volumeButtonPagingKey
        }
    }

    enum class PreferenceType {
        DARK_MODE,
        DYNAMIC_COLOR,
        CONFIRM_DIALOGS,
        VOLUME_BTN_PAGING
    }
}