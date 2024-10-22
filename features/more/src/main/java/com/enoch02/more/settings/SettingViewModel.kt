package com.enoch02.more.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

//TODO: move to settings module?
@HiltViewModel
class SettingViewModel @Inject constructor(private val application: Application) : ViewModel() {
    val darkModeKey = booleanPreferencesKey("dark_mode")
    val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    //TODO: connect value to relevant functions
    val confirmDialogKey = booleanPreferencesKey("confirm_dialogs")

    fun switchBooleanPreference(key: Preferences.Key<Boolean>, newValue: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            application.applicationContext.dataStore.edit { settings ->
                settings[key] = newValue
            }
        }
    }

    fun getBooleanPreference(key: Preferences.Key<Boolean>): Flow<Boolean> {
        val flow: Flow<Boolean> =
            application.applicationContext.dataStore.data.map { preferences ->
                preferences[key] ?: false
            }
        return flow
    }
}