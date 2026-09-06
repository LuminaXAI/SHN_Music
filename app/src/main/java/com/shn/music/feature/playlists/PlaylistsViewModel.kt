package com.shn.music.feature.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.PlaylistGroup
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val repository: PlaylistRepository,
    private val playerController: SHNMusicPlayerController
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistGroup>> =
        repository.observePlaylists()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun createPlaylist(
        name: String,
        onCreated: (Long) -> Unit = {}
    ) {
        val cleanName = name.trim()

        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            val id = repository.createPlaylist(cleanName)
            onCreated(id)
        }
    }

    fun renamePlaylist(
        playlistId: Long,
        name: String
    ) {
        val cleanName = name.trim()

        if (cleanName.isEmpty()) return

        viewModelScope.launch {
            repository.renamePlaylist(
                playlistId = playlistId,
                name = cleanName
            )
        }
    }

    fun deletePlaylist(
        playlistId: Long
    ) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun loadSongs(
        playlistId: Long,
        onResult: (List<SongEntity>) -> Unit
    ) {
        viewModelScope.launch {
            onResult(
                repository.getSongs(playlistId)
            )
        }
    }

    fun addSong(
        playlistId: Long,
        song: SongEntity
    ) {
        viewModelScope.launch {
            repository.addSong(
                playlistId = playlistId,
                song = song
            )
        }
    }

    fun removeSong(
        playlistId: Long,
        songId: Long
    ) {
        viewModelScope.launch {
            repository.removeSong(
                playlistId = playlistId,
                songId = songId
            )
        }
    }

    fun playPlaylist(
        songs: List<SongEntity>,
        index: Int = 0
    ) {
        if (songs.isEmpty()) return

        playerController.playQueue(
            songs = songs,
            index = index.coerceIn(
                0,
                songs.lastIndex
            )
        )
    }
}
