package com.shn.music.domain.repository

import com.shn.music.core.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun observeSongs(): Flow<List<SongEntity>>
    fun search(query: String): Flow<List<SongEntity>>
    suspend fun scanAndSync()
}
