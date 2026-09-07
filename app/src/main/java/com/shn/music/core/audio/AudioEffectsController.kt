package com.shn.music.core.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer

/** Owns platform effects and never claims an effect is available when construction fails. */
class AudioEffectsController(private val audioSessionId: Int) {
    private val bass = runCatching { BassBoost(0, audioSessionId) }.getOrNull()
    private val virtualizer = runCatching { Virtualizer(0, audioSessionId) }.getOrNull()
    private val reverb = runCatching { PresetReverb(0, audioSessionId) }.getOrNull()
    val bassSupported get() = bass?.strengthSupported == true
    val virtualizerSupported get() = virtualizer?.strengthSupported == true
    val reverbSupported get() = reverb != null
    fun apply(settings: AudioSettings) {
        bass?.let { it.enabled = settings.bassEnabled && bassSupported; if (bassSupported) it.setStrength(settings.bassStrength.toShort()) }
        virtualizer?.let { it.enabled = settings.virtualizerEnabled && virtualizerSupported; if (virtualizerSupported) it.setStrength(settings.virtualizerStrength.toShort()) }
        reverb?.let { it.enabled = settings.reverbPreset > 0; if (settings.reverbPreset > 0) it.preset = settings.reverbPreset.toShort() }
    }
    fun release() { bass?.release(); virtualizer?.release(); reverb?.release() }
}
