package com.shn.music.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.shn.music.core.database.dao.PlaylistDao
import com.shn.music.core.database.dao.SongDao
import com.shn.music.core.database.entity.PlaylistEntity
import com.shn.music.core.database.entity.PlaylistSongEntity
import com.shn.music.core.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SHNMusicDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    abstract fun playlistDao(): PlaylistDao
}
