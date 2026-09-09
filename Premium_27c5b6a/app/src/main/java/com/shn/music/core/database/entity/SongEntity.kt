package com.shn.music.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index("title"),
        Index("artist"),
        Index("album"),
        Index("albumArtist"),
        Index("genre"),
        Index("dateAdded"),
        Index("dateModified"),
        Index("isFavorite"),
        Index("playCount"),
        Index("lastPlayed")
    ]
)
data class SongEntity(
    @PrimaryKey
    val id: Long,

    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val genre: String,
    val composer: String,

    val duration: Long,
    val trackNumber: Int,
    val year: Int,

    val uri: String,
    val displayName: String,
    val relativePath: String,
    val artworkUri: String,

    val dateAdded: Long,
    val dateModified: Long,
    val size: Long,

    val isFavorite: Boolean = false,
    val playCount: Long = 0L,
    val lastPlayed: Long = 0L,
    val resumePosition: Long = 0L
)
