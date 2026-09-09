package com.shn.music.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository

class SongsViewModelFactory(private val repository: SongRepository, private val playerController: SHNMusicPlayerController) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongsViewModel::class.java)) return SongsViewModel(repository, playerController) as T
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
