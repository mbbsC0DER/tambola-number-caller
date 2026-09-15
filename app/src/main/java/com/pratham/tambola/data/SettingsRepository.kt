package com.pratham.tambola.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.emptyPreferences

private val Context.settingsStore by preferencesDataStore("settings")
enum class ThemePreference { SYSTEM, LIGHT, DARK }

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore
    private val themeKey = stringPreferencesKey("theme")
    val theme = store.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map {
        runCatching { ThemePreference.valueOf(it[themeKey] ?: "SYSTEM") }.getOrDefault(ThemePreference.SYSTEM)
    }
    suspend fun setTheme(theme: ThemePreference) { store.edit { it[themeKey] = theme.name } }
}
