package com.shn.music.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.AlbumGroup
import com.shn.music.core.database.model.ArtistGroup
import com.shn.music.core.database.model.FolderGroup
import com.shn.music.core.database.model.GenreGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("""
        SELECT * FROM songs
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun observeAll(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE isFavorite = 1
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun observeFavorites(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE lastPlayed > 0
        ORDER BY lastPlayed DESC
    """)
    fun observeRecentlyPlayed(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE playCount > 0
        ORDER BY playCount DESC, lastPlayed DESC
    """)
    fun observeMostPlayed(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        ORDER BY dateAdded DESC, title COLLATE NOCASE ASC
    """)
    fun observeRecentlyAdded(): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE title LIKE '%' || :query || '%'
           OR artist LIKE '%' || :query || '%'
           OR album LIKE '%' || :query || '%'
           OR albumArtist LIKE '%' || :query || '%'
           OR genre LIKE '%' || :query || '%'
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun search(query: String): Flow<List<SongEntity>>

    @Query("""
        SELECT
            album AS name,
            albumArtist AS artist,
            COUNT(*) AS songCount,
            MIN(artworkUri) AS artworkUri
        FROM songs
        WHERE TRIM(album) != ''
        GROUP BY album, albumArtist
        ORDER BY album COLLATE NOCASE ASC
    """)
    fun observeAlbums(): Flow<List<AlbumGroup>>

    @Query("""
        SELECT
            artist AS name,
            COUNT(*) AS songCount
        FROM songs
        WHERE TRIM(artist) != ''
        GROUP BY artist
        ORDER BY artist COLLATE NOCASE ASC
    """)
    fun observeArtists(): Flow<List<ArtistGroup>>

    @Query("""
        SELECT
            genre AS name,
            COUNT(*) AS songCount
        FROM songs
        WHERE TRIM(genre) != ''
        GROUP BY genre
        ORDER BY genre COLLATE NOCASE ASC
    """)
    fun observeGenres(): Flow<List<GenreGroup>>

    @Query("""
        SELECT
            CASE
                WHEN TRIM(relativePath) = '' THEN 'Music'
                ELSE
                    TRIM(
                        substr(
                            relativePath,
                            1,
                            CASE
                                WHEN instr(relativePath, '/') > 0
                                THEN instr(relativePath, '/') - 1
                                ELSE length(relativePath)
                            END
                        )
                    )
            END AS name,
            COUNT(*) AS songCount
        FROM songs
        GROUP BY name
        ORDER BY name COLLATE NOCASE ASC
    """)
    fun observeFolders(): Flow<List<FolderGroup>>

    @Query("""
        SELECT * FROM songs
        WHERE album = :album
          AND albumArtist = :albumArtist
        ORDER BY trackNumber ASC, title COLLATE NOCASE ASC
    """)
    fun observeSongsByAlbum(
        album: String,
        albumArtist: String
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE artist = :artist
        ORDER BY album COLLATE NOCASE ASC,
                 trackNumber ASC,
                 title COLLATE NOCASE ASC
    """)
    fun observeSongsByArtist(
        artist: String
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE genre = :genre
        ORDER BY artist COLLATE NOCASE ASC,
                 album COLLATE NOCASE ASC,
                 title COLLATE NOCASE ASC
    """)
    fun observeSongsByGenre(
        genre: String
    ): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE
            CASE
                WHEN TRIM(:folder) = 'Music'
                THEN TRIM(relativePath) = ''
                     OR TRIM(relativePath) LIKE 'Music/%'
                ELSE
                    TRIM(
                        substr(
                            relativePath,
                            1,
                            CASE
                                WHEN instr(relativePath, '/') > 0
                                THEN instr(relativePath, '/') - 1
                                ELSE length(relativePath)
                            END
                        )
                    ) = :folder
            END
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun observeSongsByFolder(folder: String): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs
        WHERE album = :album
          AND albumArtist = :albumArtist
        ORDER BY trackNumber ASC, title COLLATE NOCASE ASC
    """)
    suspend fun getSongsByAlbum(
        album: String,
        albumArtist: String
    ): List<SongEntity>

    @Query("""
        SELECT * FROM songs
        WHERE artist = :artist
        ORDER BY album COLLATE NOCASE ASC,
                 trackNumber ASC,
                 title COLLATE NOCASE ASC
    """)
    suspend fun getSongsByArtist(
        artist: String
    ): List<SongEntity>

    @Query("""
        SELECT * FROM songs
        WHERE genre = :genre
        ORDER BY artist COLLATE NOCASE ASC,
                 album COLLATE NOCASE ASC,
                 title COLLATE NOCASE ASC
    """)
    suspend fun getSongsByGenre(
        genre: String
    ): List<SongEntity>

    @Query("""
        SELECT * FROM songs
        WHERE
            CASE
                WHEN TRIM(:folder) = 'Music'
                THEN TRIM(relativePath) = ''
                     OR TRIM(relativePath) LIKE 'Music/%'
                ELSE
                    TRIM(
                        substr(
                            relativePath,
                            1,
                            CASE
                                WHEN instr(relativePath, '/') > 0
                                THEN instr(relativePath, '/') - 1
                                ELSE length(relativePath)
                            END
                        )
                    ) = :folder
            END
        ORDER BY title COLLATE NOCASE ASC
    """)
    suspend fun getSongsByFolder(
        folder: String
    ): List<SongEntity>

    @Query("SELECT COUNT(*) FROM songs")
    fun observeCount(): Flow<Int>

    @Query("""
        SELECT * FROM songs
        WHERE uri = :uri
        LIMIT 1
    """)
    suspend fun findByUri(uri: String): SongEntity?

    @Query("""
        UPDATE songs
        SET isFavorite = :favorite
        WHERE id = :id
    """)
    suspend fun setFavorite(
        id: Long,
        favorite: Boolean
    )

    @Query("""
        UPDATE songs
        SET
            playCount = playCount + 1,
            lastPlayed = :timestamp,
            resumePosition = 0
        WHERE id = :id
    """)
    suspend fun recordPlay(
        id: Long,
        timestamp: Long
    )

    @Query("""
        UPDATE songs
        SET resumePosition = :position
        WHERE id = :id
    """)
    suspend fun updateResumePosition(
        id: Long,
        position: Long
    )

    @Query("""
        UPDATE songs
        SET resumePosition = 0
        WHERE id = :id
    """)
    suspend fun clearResumePosition(
        id: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(songs: List<SongEntity>) {
        deleteAll()

        if (songs.isNotEmpty()) {
            insertAll(songs)
        }
    }
}
