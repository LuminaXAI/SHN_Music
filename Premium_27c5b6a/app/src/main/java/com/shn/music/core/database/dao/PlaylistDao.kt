package com.shn.music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shn.music.core.database.entity.PlaylistEntity
import com.shn.music.core.database.entity.PlaylistSongEntity
import com.shn.music.core.database.model.PlaylistGroup
import com.shn.music.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("""
        SELECT
            p.id AS id,
            p.name AS name,
            COUNT(ps.songId) AS songCount
        FROM playlists p
        LEFT JOIN playlist_songs ps
            ON p.id = ps.playlistId
        GROUP BY p.id, p.name
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun observePlaylists(): Flow<List<PlaylistGroup>>

    @Query("""
        SELECT *
        FROM playlists
        WHERE id = :playlistId
        LIMIT 1
    """)
    suspend fun getPlaylist(
        playlistId: Long
    ): PlaylistEntity?

    @Query("""
        SELECT s.*
        FROM songs s
        INNER JOIN playlist_songs ps
            ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC, s.title COLLATE NOCASE ASC
    """)
    suspend fun getSongs(
        playlistId: Long
    ): List<SongEntity>

    @Insert
    suspend fun createPlaylist(
        playlist: PlaylistEntity
    ): Long

    @Query("""
        UPDATE playlists
        SET
            name = :name,
            updatedAt = :updatedAt
        WHERE id = :playlistId
    """)
    suspend fun renamePlaylist(
        playlistId: Long,
        name: String,
        updatedAt: Long
    )

    @Query("""
        DELETE FROM playlists
        WHERE id = :playlistId
    """)
    suspend fun deletePlaylist(
        playlistId: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(
        song: PlaylistSongEntity
    )

    @Query("""
        DELETE FROM playlist_songs
        WHERE playlistId = :playlistId
          AND songId = :songId
    """)
    suspend fun removeSong(
        playlistId: Long,
        songId: Long
    )

    @Query("""
        DELETE FROM playlist_songs
        WHERE playlistId = :playlistId
    """)
    suspend fun clearPlaylist(
        playlistId: Long
    )

    @Query("""
        SELECT COALESCE(MAX(position), -1) + 1
        FROM playlist_songs
        WHERE playlistId = :playlistId
    """)
    suspend fun nextPosition(
        playlistId: Long
    ): Int
}
