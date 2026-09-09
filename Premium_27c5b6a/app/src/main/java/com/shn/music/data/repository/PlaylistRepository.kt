package com.shn.music.data.repository

import com.shn.music.core.database.dao.PlaylistDao
import com.shn.music.core.database.entity.PlaylistEntity
import com.shn.music.core.database.entity.PlaylistSongEntity
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.PlaylistGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaylistRepository(
    private val dao: PlaylistDao
) {

    fun observePlaylists(): Flow<List<PlaylistGroup>> =
        dao.observePlaylists()

    suspend fun createPlaylist(
        name: String
    ): Long =
        withContext(Dispatchers.IO) {
            dao.createPlaylist(
                PlaylistEntity(
                    name = name.trim()
                )
            )
        }

    suspend fun renamePlaylist(
        playlistId: Long,
        name: String
    ) {
        withContext(Dispatchers.IO) {
            dao.renamePlaylist(
                playlistId = playlistId,
                name = name.trim(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun deletePlaylist(
        playlistId: Long
    ) {
        withContext(Dispatchers.IO) {
            dao.deletePlaylist(playlistId)
        }
    }

    suspend fun getSongs(
        playlistId: Long
    ): List<SongEntity> =
        withContext(Dispatchers.IO) {
            dao.getSongs(playlistId)
        }

    suspend fun addSong(
        playlistId: Long,
        song: SongEntity
    ) {
        withContext(Dispatchers.IO) {
            val position = dao.nextPosition(playlistId)

            dao.insertPlaylistSong(
                PlaylistSongEntity(
                    playlistId = playlistId,
                    songId = song.id,
                    position = position
                )
            )
        }
    }

    suspend fun removeSong(
        playlistId: Long,
        songId: Long
    ) {
        withContext(Dispatchers.IO) {
            dao.removeSong(
                playlistId = playlistId,
                songId = songId
            )
        }
    }

    suspend fun clearPlaylist(
        playlistId: Long
    ) {
        withContext(Dispatchers.IO) {
            dao.clearPlaylist(playlistId)
        }
    }
}
