package com.shn.music.core.media.service

import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.common.Player
import com.shn.music.core.audio.AudioEffectsController
import com.shn.music.core.audio.AudioSettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class MusicPlaybackService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var effects: AudioEffectsController? = null
    private var effectsSettingsJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setPauseAtEndOfMediaItems(false)
            .build()

        val settingsStore = AudioSettingsStore(this)
        player!!.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                effects?.release()
                effects = if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) AudioEffectsController(audioSessionId) else null
                effectsSettingsJob?.cancel()
                effectsSettingsJob = serviceScope.launch { settingsStore.settings.collectLatest { effects?.apply(it) } }
            }
        })

        mediaSession = MediaSession.Builder(
            this,
            player!!
        ).build()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (player?.isPlaying != true) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null

        effects?.release()
        effects = null
        effectsSettingsJob?.cancel()
        player?.release()
        player = null

        super.onDestroy()
    }
}
