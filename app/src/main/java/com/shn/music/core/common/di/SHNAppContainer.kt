package com.shn.music.core.common.di

import android.content.Context
import com.shn.music.core.database.DatabaseProvider
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.core.media.queue.QueueStore
import com.shn.music.core.media.scanner.MusicScanner
import com.shn.music.data.repository.PlaylistRepository
import com.shn.music.data.repository.SongRepository

class SHNAppContainer(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val database =
        DatabaseProvider.get(appContext)

    private val scanner =
        MusicScanner(appContext)

    private val queueStore =
        QueueStore(appContext)

    val songRepository =
        SongRepository(
            dao = database.songDao(),
            scanner = scanner
        )

    val playlistRepository =
        PlaylistRepository(
            dao = database.playlistDao()
        )

    val playerController =
        SHNMusicPlayerController(
            context = appContext,
            queueStore = queueStore,
            songDao = database.songDao()
        )
}
