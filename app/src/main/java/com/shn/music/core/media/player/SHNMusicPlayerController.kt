package com.shn.music.core.media.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.shn.music.core.database.dao.SongDao
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.media.queue.QueueSnapshot
import com.shn.music.core.media.queue.QueueStore
import com.shn.music.core.media.service.MusicPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QueueItemUi(
    val index: Int,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val artworkUri: String,
    val isCurrent: Boolean
)

data class PlayerUiState(
    val connected: Boolean = false,
    val uri: String = "",
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: String = "",
    val position: Long = 0L,
    val duration: Long = 0L,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

class SHNMusicPlayerController(
    context: Context,
    private val queueStore: QueueStore,
    private val songDao: SongDao
) {

    private val appContext =
        context.applicationContext

    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Main.immediate
        )

    private val handler =
        Handler(Looper.getMainLooper())

    private var controller: MediaController? = null
    private var recordedPlayUri: String? = null
    private var lastResumePersistElapsed: Long = 0L

    private var controllerFuture:
        ListenableFuture<MediaController>? = null

    private val _state =
        MutableStateFlow(PlayerUiState())

    val state: StateFlow<PlayerUiState> =
        _state.asStateFlow()

    private val _queue =
        MutableStateFlow<List<QueueItemUi>>(
            emptyList()
        )

    val queue: StateFlow<List<QueueItemUi>> =
        _queue.asStateFlow()

    private val _queueRestored =
        MutableStateFlow(false)

    val queueRestored: StateFlow<Boolean> =
        _queueRestored.asStateFlow()

    private val listener =
        object : Player.Listener {

            override fun onEvents(
                player: Player,
                events: Player.Events
            ) {
                publishState(player)
                publishQueue(player)

                if (
                    events.contains(
                        Player.EVENT_MEDIA_ITEM_TRANSITION
                    ) ||
                    events.contains(
                        Player.EVENT_IS_PLAYING_CHANGED
                    )
                ) {
                    recordCurrentPlayIfNeeded(player)
                }

                persistSnapshot(player)
            }
        }

    private val positionUpdater =
        object : Runnable {

            override fun run() {

                controller?.let { player ->
                    publishState(player)

                    val now = SystemClock.elapsedRealtime()
                    if (now - lastResumePersistElapsed >= 5000L) {
                        persistCurrentResumePosition()
                        lastResumePersistElapsed = now
                    }
                }

                handler.postDelayed(
                    this,
                    1000L
                )
            }
        }

    init {
        connect()
        handler.post(positionUpdater)
    }

    private fun connect() {

        val token =
            SessionToken(
                appContext,
                ComponentName(
                    appContext,
                    MusicPlaybackService::class.java
                )
            )

        val future =
            MediaController.Builder(
                appContext,
                token
            ).buildAsync()

        controllerFuture = future

        future.addListener(
            {
                try {

                    val mediaController =
                        future.get()

                    controller =
                        mediaController

                    mediaController.addListener(
                        listener
                    )

                    publishState(
                        mediaController
                    )

                    publishQueue(
                        mediaController
                    )

                } catch (_: Exception) {
                    controller = null
                }
            },
            ContextCompat.getMainExecutor(
                appContext
            )
        )
    }

    suspend fun restoreQueue(
        library: List<SongEntity>
    ) {

        if (_queueRestored.value) {
            return
        }

        val player =
            controller ?: return

        if (player.mediaItemCount > 0) {
            _queueRestored.value = true
            publishState(player)
            publishQueue(player)
            return
        }

        val snapshot =
            queueStore.load()

        if (snapshot.uris.isEmpty()) {
            _queueRestored.value = true
            return
        }

        val byUri =
            library.associateBy {
                it.uri
            }

        val songs =
            snapshot.uris.mapNotNull {
                byUri[it]
            }

        if (songs.isEmpty()) {
            _queueRestored.value = true
            return
        }

        val items =
            songs.map(::toMediaItem)

        val index =
            snapshot.currentIndex.coerceIn(
                0,
                items.lastIndex
            )

        player.setMediaItems(
            items,
            index,
            snapshot.position.coerceAtLeast(0L)
        )

        player.shuffleModeEnabled =
            snapshot.shuffleEnabled

        player.repeatMode =
            normalizeRepeatMode(
                snapshot.repeatMode
            )

        player.prepare()

        player.playWhenReady =
            snapshot.playWhenReady

        _queueRestored.value = true

        publishState(player)
        publishQueue(player)
    }

    fun playQueue(
        songs: List<SongEntity>,
        index: Int
    ) {

        val player =
            controller ?: return

        if (songs.isEmpty()) return
        if (index !in songs.indices) return

        player.setMediaItems(
            songs.map(::toMediaItem),
            index,
            0L
        )

        player.prepare()
        player.play()

        publishState(player)
        publishQueue(player)
        persistSnapshot(player)
    }

    fun addToQueue(
        song: SongEntity
    ) {
        controller?.let { player ->

            player.addMediaItem(
                toMediaItem(song)
            )

            publishQueue(player)
            persistSnapshot(player)
        }
    }

    fun playNext(
        song: SongEntity
    ) {
        controller?.let { player ->

            val index =
                if (player.currentMediaItemIndex >= 0) {
                    player.currentMediaItemIndex + 1
                } else {
                    player.mediaItemCount
                }

            player.addMediaItem(
                index.coerceAtMost(
                    player.mediaItemCount
                ),
                toMediaItem(song)
            )

            publishQueue(player)
            persistSnapshot(player)
        }
    }

    fun removeFromQueue(
        index: Int
    ) {
        controller?.let { player ->

            if (
                index in 0 until player.mediaItemCount
            ) {
                player.removeMediaItem(index)
                publishQueue(player)
                publishState(player)
                persistSnapshot(player)
            }
        }
    }

    fun moveQueueItem(
        from: Int,
        to: Int
    ) {
        controller?.let { player ->

            if (
                from !in 0 until player.mediaItemCount ||
                to !in 0 until player.mediaItemCount
            ) {
                return
            }

            if (from == to) {
                return
            }

            player.moveMediaItem(
                from,
                to
            )

            publishQueue(player)
            publishState(player)
            persistSnapshot(player)
        }
    }

    fun clearQueue() {
        controller?.let { player ->

            player.clearMediaItems()

            publishQueue(player)
            publishState(player)

            scope.launch {
                queueStore.clear()
            }
        }
    }

    fun toggleShuffle() {
        controller?.let { player ->

            player.shuffleModeEnabled =
                !player.shuffleModeEnabled

            publishState(player)
            persistSnapshot(player)
        }
    }

    fun cycleRepeatMode() {
        controller?.let { player ->

            player.repeatMode =
                when (player.repeatMode) {

                    Player.REPEAT_MODE_OFF ->
                        Player.REPEAT_MODE_ALL

                    Player.REPEAT_MODE_ALL ->
                        Player.REPEAT_MODE_ONE

                    else ->
                        Player.REPEAT_MODE_OFF
                }

            publishState(player)
            persistSnapshot(player)
        }
    }

    fun play() {
        controller?.play()
    }

    fun pause() {
        controller?.pause()
    }

    fun togglePlayPause() {
        controller?.let { player ->

            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }

    fun next() {
        controller?.seekToNextMediaItem()
    }

    fun previous() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(
        position: Long
    ) {
        controller?.seekTo(
            position.coerceAtLeast(0L)
        )
    }

    fun selectQueueItem(
        index: Int
    ) {
        controller?.let { player ->

            if (
                index !in 0 until player.mediaItemCount
            ) {
                return
            }

            player.seekToDefaultPosition(index)
            player.prepare()
            player.play()

            publishState(player)
            publishQueue(player)
            persistSnapshot(player)
        }
    }

    fun release() {

        persistCurrentResumePosition()
        handler.removeCallbacksAndMessages(
            null
        )

        controller?.removeListener(
            listener
        )

        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }

        controller = null
        controllerFuture = null
    }

    private fun toMediaItem(
        song: SongEntity
    ): MediaItem {

        return MediaItem.Builder()
            .setMediaId(song.uri)
            .setUri(song.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .apply {

                        if (
                            song.artworkUri.isNotBlank()
                        ) {
                            setArtworkUri(
                                Uri.parse(
                                    song.artworkUri
                                )
                            )
                        }
                    }
                    .build()
            )
            .build()
    }

    private fun publishState(
        player: Player
    ) {

        val item =
            player.currentMediaItem

        val metadata =
            item?.mediaMetadata

        _state.value =
            PlayerUiState(
                connected = true,
                uri = item?.mediaId.orEmpty(),
                isPlaying =
                    player.isPlaying,
                currentIndex =
                    player.currentMediaItemIndex,
                title =
                    metadata?.title
                        ?.toString()
                        .orEmpty(),
                artist =
                    metadata?.artist
                        ?.toString()
                        .orEmpty(),
                album =
                    metadata?.albumTitle
                        ?.toString()
                        .orEmpty(),
                artworkUri =
                    metadata?.artworkUri
                        ?.toString()
                        .orEmpty(),
                position =
                    player.currentPosition
                        .coerceAtLeast(0L),
                duration =
                    player.duration
                        .takeIf {
                            it > 0L
                        }
                        ?: 0L,
                hasNext =
                    player.hasNextMediaItem(),
                hasPrevious =
                    player.hasPreviousMediaItem(),
                shuffleEnabled =
                    player.shuffleModeEnabled,
                repeatMode =
                    normalizeRepeatMode(
                        player.repeatMode
                    )
            )
    }

    private fun publishQueue(
        player: Player
    ) {

        val result =
            mutableListOf<QueueItemUi>()

        repeat(
            player.mediaItemCount
        ) { index ->

            val item =
                player.getMediaItemAt(index)

            val metadata =
                item.mediaMetadata

            result +=
                QueueItemUi(
                    index = index,
                    uri = item.mediaId,
                    title =
                        metadata.title
                            ?.toString()
                            .orEmpty(),
                    artist =
                        metadata.artist
                            ?.toString()
                            .orEmpty(),
                    album =
                        metadata.albumTitle
                            ?.toString()
                            .orEmpty(),
                    artworkUri =
                        metadata.artworkUri
                            ?.toString()
                            .orEmpty(),
                    isCurrent =
                        index ==
                            player.currentMediaItemIndex
                )
        }

        _queue.value = result
    }

    private fun persistSnapshot(
        player: Player
    ) {

        val uris =
            buildList {

                repeat(
                    player.mediaItemCount
                ) { index ->

                    add(
                        player.getMediaItemAt(
                            index
                        ).mediaId
                    )
                }
            }

        val snapshot =
            QueueSnapshot(
                uris = uris,
                currentIndex =
                    player.currentMediaItemIndex
                        .coerceAtLeast(0),
                position =
                    player.currentPosition
                        .coerceAtLeast(0L),
                shuffleEnabled =
                    player.shuffleModeEnabled,
                repeatMode =
                    normalizeRepeatMode(
                        player.repeatMode
                    ),
                playWhenReady =
                    player.playWhenReady
            )

        scope.launch {
            queueStore.save(snapshot)
        }
    }

    private fun recordCurrentPlayIfNeeded(
        player: Player
    ) {

        val item =
            player.currentMediaItem
                ?: return

        val uri =
            item.mediaId

        if (
            uri.isBlank() ||
            !player.isPlaying
        ) {
            return
        }

        if (
            recordedPlayUri == uri
        ) {
            return
        }

        recordedPlayUri = uri

        scope.launch(
            Dispatchers.IO
        ) {
            val song =
                songDao.findByUri(uri)

            if (song != null) {
                songDao.recordPlay(
                    id = song.id,
                    timestamp =
                        System.currentTimeMillis()
                )
            }
        }
    }

    private fun persistCurrentResumePosition() {

        val player =
            controller ?: return

        val item =
            player.currentMediaItem
                ?: return

        val uri =
            item.mediaId

        if (uri.isBlank()) {
            return
        }

        val position =
            player.currentPosition
                .coerceAtLeast(0L)

        scope.launch(
            Dispatchers.IO
        ) {
            val song =
                songDao.findByUri(uri)

            if (song != null) {
                songDao.updateResumePosition(
                    id = song.id,
                    position = position
                )
            }
        }
    }

    private fun normalizeRepeatMode(
        mode: Int
    ): Int {

        return when (mode) {

            Player.REPEAT_MODE_OFF,
            Player.REPEAT_MODE_ONE,
            Player.REPEAT_MODE_ALL ->
                mode

            else ->
                Player.REPEAT_MODE_OFF
        }
    }
}
