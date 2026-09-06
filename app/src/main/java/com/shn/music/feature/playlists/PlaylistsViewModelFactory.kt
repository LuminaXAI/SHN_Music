package com.shn.music.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.PlaylistRepository

class PlaylistsViewModelFactory(
    private val repository: PlaylistRepository,
    private val playerController: SHNMusicPlayerController
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(PlaylistsViewModel::class.java)) {
            return PlaylistsViewModel(
                repository = repository,
                playerController = playerController
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
