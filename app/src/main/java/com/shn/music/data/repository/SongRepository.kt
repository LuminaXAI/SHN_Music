package com.shn.music.data.repository

import com.shn.music.core.database.dao.SongDao
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.AlbumGroup
import com.shn.music.core.database.model.ArtistGroup
import com.shn.music.core.database.model.FolderGroup
import com.shn.music.core.database.model.GenreGroup
import com.shn.music.core.media.scanner.MusicScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(
    val dao: SongDao,
    private val scanner: MusicScanner
) {

    fun observeSongs(): Flow<List<SongEntity>> =
        dao.observeAll()

    fun observeFavorites(): Flow<List<SongEntity>> =
        dao.observeFavorites()

    fun observeRecentlyPlayed(): Flow<List<SongEntity>> =
        dao.observeRecentlyPlayed()

    fun observeMostPlayed(): Flow<List<SongEntity>> =
        dao.observeMostPlayed()

    fun observeRecentlyAdded(): Flow<List<SongEntity>> =
        dao.observeRecentlyAdded()

    fun observeAlbums(): Flow<List<AlbumGroup>> =
        dao.observeAlbums()

    fun observeArtists(): Flow<List<ArtistGroup>> =
        dao.observeArtists()

    fun observeGenres(): Flow<List<GenreGroup>> =
        dao.observeGenres()

    fun observeFolders(): Flow<List<FolderGroup>> =
        dao.observeFolders()

    fun observeSongsByAlbum(
        album: String,
        albumArtist: String
    ): Flow<List<SongEntity>> =
        dao.observeSongsByAlbum(album, albumArtist)

    fun observeSongsByArtist(
        artist: String
    ): Flow<List<SongEntity>> =
        dao.observeSongsByArtist(artist)

    fun observeSongsByGenre(
        genre: String
    ): Flow<List<SongEntity>> =
        dao.observeSongsByGenre(genre)

    fun observeSongsByFolder(
        folder: String
    ): Flow<List<SongEntity>> =
        dao.observeSongsByFolder(folder)

    suspend fun getSongsByAlbum(
        album: String,
        albumArtist: String
    ): List<SongEntity> =
        withContext(Dispatchers.IO) {
            dao.getSongsByAlbum(album, albumArtist)
        }

    suspend fun getSongsByArtist(
        artist: String
    ): List<SongEntity> =
        withContext(Dispatchers.IO) {
            dao.getSongsByArtist(artist)
        }

    suspend fun getSongsByGenre(
        genre: String
    ): List<SongEntity> =
        withContext(Dispatchers.IO) {
            dao.getSongsByGenre(genre)
        }

    suspend fun getSongsByFolder(
        folder: String
    ): List<SongEntity> =
        withContext(Dispatchers.IO) {
            dao.getSongsByFolder(folder)
        }

    fun observeCount(): Flow<Int> =
        dao.observeCount()

    fun search(query: String): Flow<List<SongEntity>> =
        dao.search(query.trim())

    suspend fun getSongByUri(uri: String): SongEntity? = withContext(Dispatchers.IO) { dao.findByUri(uri) }

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val songs = scanner.scan()
            dao.replaceAll(songs)
        }
    }

    suspend fun setFavorite(
        id: Long,
        favorite: Boolean
    ) {
        withContext(Dispatchers.IO) {
            dao.setFavorite(id, favorite)
        }
    }

    suspend fun recordPlay(uri: String) {
        withContext(Dispatchers.IO) {
            val song = dao.findByUri(uri)

            if (song != null) {
                dao.recordPlay(
                    id = song.id,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun saveResumePosition(
        uri: String,
        position: Long
    ) {
        withContext(Dispatchers.IO) {
            val song = dao.findByUri(uri)

            if (song != null) {
                dao.updateResumePosition(
                    id = song.id,
                    position = position.coerceAtLeast(0L)
                )
            }
        }
    }

    suspend fun clearResumePosition(uri: String) {
        withContext(Dispatchers.IO) {
            val song = dao.findByUri(uri)

            if (song != null) {
                dao.clearResumePosition(song.id)
            }
        }
    }
}
