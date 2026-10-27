package com.yadyar.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "mono_prefs")

class ThemePreference private constructor(private val context: Context) {
    companion object {
        private val KEY_DARK = booleanPreferencesKey("is_dark")
        private val KEY_DEMO_MODE = booleanPreferencesKey("demo_mode")
        @Volatile private var INSTANCE: ThemePreference? = null
        fun get(context: Context): ThemePreference =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThemePreference(context.applicationContext).also { INSTANCE = it }
            }
    }

    val isDark: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK] ?: false }
    val isDemoMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_DEMO_MODE] ?: false }

    suspend fun setDark(value: Boolean) {
        context.dataStore.edit { it[KEY_DARK] = value }
    }
    suspend fun setDemoMode(value: Boolean) {
        context.dataStore.edit { it[KEY_DEMO_MODE] = value }
    }
}
