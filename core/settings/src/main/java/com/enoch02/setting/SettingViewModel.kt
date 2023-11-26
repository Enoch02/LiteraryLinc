package com.enoch02.setting

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingViewModel @Inject constructor(private val application: Application) : ViewModel() {
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")
    //TODO: connect value to relevant functions
    private val animationKey = booleanPreferencesKey("animations")

    fun switchDarkModeValue(newValue: Boolean) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { settings ->
                settings[darkModeKey] = newValue
            }
        }
    }

    fun getDarkModeValue(): Flow<Boolean> {
        val darkModeFlow: Flow<Boolean> =
            application.applicationContext.dataStore.data.map { preferences ->
                preferences[darkModeKey] ?: false
            }
        return darkModeFlow
    }

    fun switchDynamicColorValue(newValue: Boolean) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { settings ->
                settings[dynamicColorKey] = newValue
            }
        }
    }

    fun getDynamicColorValue(): Flow<Boolean> {
        val dynamicColorFlow: Flow<Boolean> =
            application.applicationContext.dataStore.data.map { preferences ->
                preferences[dynamicColorKey] ?: true
            }
        return dynamicColorFlow
    }

    fun switchAnimationsValue(newValue: Boolean) {
        viewModelScope.launch {
            application.applicationContext.dataStore.edit { settings ->
                settings[animationKey] = newValue
            }
        }
    }

    fun getAnimationsValue(): Flow<Boolean> {
        val dynamicColorFlow: Flow<Boolean> =
            application.applicationContext.dataStore.data.map { preferences ->
                preferences[animationKey] ?: true
            }
        return dynamicColorFlow
    }
}