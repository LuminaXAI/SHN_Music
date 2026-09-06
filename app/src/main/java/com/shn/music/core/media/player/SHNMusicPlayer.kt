package com.shn.music.core.media.player

import androidx.media3.exoplayer.ExoPlayer

class SHNMusicPlayer(
    private val player: ExoPlayer
) {

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    fun isPlaying(): Boolean = player.isPlaying
}
