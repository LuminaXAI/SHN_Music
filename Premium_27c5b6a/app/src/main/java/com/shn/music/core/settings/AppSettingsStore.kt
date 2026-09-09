package com.shn.music.core.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppLanguage(val code: String) {
    ARABIC("ar"),
    ENGLISH("en");

    companion object {
        fun fromCode(value: String?): AppLanguage =
            entries.firstOrNull { it.code == value } ?: ARABIC
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val language: AppLanguage = AppLanguage.ARABIC,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val savePlaybackPosition: Boolean = true
)

private val Context.shnSettings by preferencesDataStore(name = "shn_settings")

class AppSettingsStore(context: Context) {
    private val appContext = context.applicationContext

    private object Keys {
        val language = stringPreferencesKey("language")
        val theme = stringPreferencesKey("theme")
        val savePosition = booleanPreferencesKey("save_position")
    }

    val settings: Flow<AppSettings> =
        appContext.shnSettings.data.map { prefs ->
            AppSettings(
                language = AppLanguage.fromCode(prefs[Keys.language]),
                themeMode = runCatching {
                    ThemeMode.valueOf(prefs[Keys.theme] ?: ThemeMode.SYSTEM.name)
                }.getOrDefault(ThemeMode.SYSTEM),
                savePlaybackPosition = prefs[Keys.savePosition] ?: true
            )
        }

    suspend fun setLanguage(language: AppLanguage) {
        appContext.shnSettings.edit { it[Keys.language] = language.code }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        appContext.shnSettings.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setSavePlaybackPosition(enabled: Boolean) {
        appContext.shnSettings.edit { it[Keys.savePosition] = enabled }
    }
}
