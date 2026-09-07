package com.shn.music.core.audio

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.audioSettingsDataStore by preferencesDataStore("shn_audio_studio")

data class AudioSettings(
    val speed: Float = 1f,
    val bassEnabled: Boolean = false,
    val bassStrength: Int = 500,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 500,
    val reverbPreset: Int = 0,
    val balance: Float = 0f
)

/** Persistent settings only; capability-dependent effects are applied by the playback service. */
class AudioSettingsStore(context: Context) {
    private val appContext = context.applicationContext
    private object Keys {
        val speed = floatPreferencesKey("speed")
        val bassEnabled = booleanPreferencesKey("bass_enabled")
        val bassStrength = intPreferencesKey("bass_strength")
        val virtualizerEnabled = booleanPreferencesKey("virtualizer_enabled")
        val virtualizerStrength = intPreferencesKey("virtualizer_strength")
        val reverbPreset = intPreferencesKey("reverb_preset")
        val balance = floatPreferencesKey("balance")
    }
    val settings: Flow<AudioSettings> = appContext.audioSettingsDataStore.data.map { p ->
        AudioSettings(p[Keys.speed] ?: 1f, p[Keys.bassEnabled] ?: false, p[Keys.bassStrength] ?: 500,
            p[Keys.virtualizerEnabled] ?: false, p[Keys.virtualizerStrength] ?: 500,
            p[Keys.reverbPreset] ?: 0, p[Keys.balance] ?: 0f)
    }
    suspend fun setSpeed(value: Float) = appContext.audioSettingsDataStore.edit { it[Keys.speed] = value.coerceIn(.5f, 2f) }
    suspend fun setBass(enabled: Boolean, strength: Int) = appContext.audioSettingsDataStore.edit { it[Keys.bassEnabled] = enabled; it[Keys.bassStrength] = strength.coerceIn(0, 1000) }
    suspend fun setVirtualizer(enabled: Boolean, strength: Int) = appContext.audioSettingsDataStore.edit { it[Keys.virtualizerEnabled] = enabled; it[Keys.virtualizerStrength] = strength.coerceIn(0, 1000) }
    suspend fun setReverbPreset(value: Int) = appContext.audioSettingsDataStore.edit { it[Keys.reverbPreset] = value.coerceAtLeast(0) }
    suspend fun setBalance(value: Float) = appContext.audioSettingsDataStore.edit { it[Keys.balance] = value.coerceIn(-1f, 1f) }
}
