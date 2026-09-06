package com.shn.music.feature.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.AlbumGroup
import com.shn.music.core.database.model.ArtistGroup
import com.shn.music.core.database.model.FolderGroup
import com.shn.music.core.database.model.GenreGroup
import com.shn.music.core.media.player.PlayerUiState
import com.shn.music.core.media.player.QueueItemUi
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SongsViewModel(private val repository: SongRepository, private val playerController: SHNMusicPlayerController) : ViewModel() {
    val songs: StateFlow<List<SongEntity>> = repository.observeSongs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites: StateFlow<List<SongEntity>> = repository.observeFavorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recent: StateFlow<List<SongEntity>> = repository.observeRecentlyPlayed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentlyPlayed = recent
    val mostPlayed: StateFlow<List<SongEntity>> = repository.observeMostPlayed().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentlyAdded: StateFlow<List<SongEntity>> = repository.observeRecentlyAdded().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val albums: StateFlow<List<AlbumGroup>> = repository.observeAlbums().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val artists: StateFlow<List<ArtistGroup>> = repository.observeArtists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val genres: StateFlow<List<GenreGroup>> = repository.observeGenres().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val folders: StateFlow<List<FolderGroup>> = repository.observeFolders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val count: StateFlow<Int> = repository.observeCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val playerState: StateFlow<PlayerUiState> = playerController.state
    val queue: StateFlow<List<QueueItemUi>> = playerController.queue

    init { viewModelScope.launch { refresh(); songs.collect { playerController.restoreQueue(it) } } }
    fun refresh() { viewModelScope.launch { repository.refresh() } }
    fun restoreQueue(library: List<SongEntity>) { viewModelScope.launch { playerController.restoreQueue(library) } }
    fun play(song: SongEntity) { playerController.playQueue(listOf(song), 0) }
    fun playSong(song: SongEntity) { play(song) }
    fun playList(list: List<SongEntity>, index: Int) { playerController.playQueue(list, index) }
    fun addToQueue(song: SongEntity) = playerController.addToQueue(song)
    fun playNext(song: SongEntity) = playerController.playNext(song)
    fun removeFromQueue(i: Int) = playerController.removeFromQueue(i)
    fun moveQueueItem(from: Int,to:Int)=playerController.moveQueueItem(from,to)
    fun clearQueue()=playerController.clearQueue()
    fun selectQueueItem(i:Int)=playerController.selectQueueItem(i)
    fun togglePlayPause()=playerController.togglePlayPause()
    fun play()=playerController.play()
    fun pause()=playerController.pause()
    fun next()=playerController.next()
    fun previous()=playerController.previous()
    fun seekTo(p:Long)=playerController.seekTo(p)
    fun toggleShuffle()=playerController.toggleShuffle()
    fun cycleRepeat()=playerController.cycleRepeatMode()
    fun loadAlbumSongs(album: AlbumGroup, onResult:(List<SongEntity>)->Unit){ viewModelScope.launch { onResult(repository.getSongsByAlbum(album.name, album.artist)) } }
    fun loadArtistSongs(artist: ArtistGroup,onResult:(List<SongEntity>)->Unit){ viewModelScope.launch { onResult(repository.getSongsByArtist(artist.name)) } }
    fun loadGenreSongs(genre: GenreGroup,onResult:(List<SongEntity>)->Unit){ viewModelScope.launch { onResult(repository.getSongsByGenre(genre.name)) } }
    fun loadFolderSongs(folder: FolderGroup,onResult:(List<SongEntity>)->Unit){ viewModelScope.launch { onResult(repository.getSongsByFolder(folder.name)) } }
}
