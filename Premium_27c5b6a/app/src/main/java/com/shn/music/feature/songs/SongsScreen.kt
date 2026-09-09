package com.shn.music.feature.songs

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.media.player.PlayerUiState
import com.shn.music.core.media.player.QueueItemUi
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsRoute(
    repository: SongRepository,
    playerController: SHNMusicPlayerController
) {

    val viewModel: SongsViewModel =
        viewModel(
            factory =
                SongsViewModelFactory(
                    repository = repository,
                    playerController =
                        playerController
                )
        )

    val songs by
        viewModel.songs
            .collectAsStateWithLifecycle()

    val count by
        viewModel.count
            .collectAsStateWithLifecycle()

    val player by
        viewModel.playerState
            .collectAsStateWithLifecycle()

    val queue by
        viewModel.queue
            .collectAsStateWithLifecycle()

    var showNowPlaying by
        remember {
            mutableStateOf(false)
        }

    var showQueue by
        remember {
            mutableStateOf(false)
        }

    LaunchedEffect(Unit) {

        if (count == 0) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(songs) {

        if (songs.isNotEmpty()) {
            viewModel.restoreQueue(songs)
        }
    }

    if (
        showNowPlaying &&
        player.title.isNotBlank()
    ) {

        NowPlayingScreen(
            state = player,
            onBack = {
                showNowPlaying = false
            },
            onPlayPause =
                viewModel::togglePlayPause,
            onPrevious =
                viewModel::previous,
            onNext =
                viewModel::next,
            onSeek =
                viewModel::seekTo,
            onShuffle =
                viewModel::toggleShuffle,
            onRepeat =
                viewModel::cycleRepeat,
            onQueue = {
                showQueue = true
            }
        )

        if (showQueue) {

            QueueDialog(
                queue = queue,
                onDismiss = {
                    showQueue = false
                },
                onSelect = {
                    viewModel.selectQueueItem(it)
                    showQueue = false
                },
                onRemove =
                    viewModel::removeFromQueue,
                onMoveUp = { index ->

                    if (index > 0) {
                        viewModel.moveQueueItem(
                            index,
                            index - 1
                        )
                    }
                },
                onMoveDown = { index ->

                    if (
                        index <
                        queue.lastIndex
                    ) {
                        viewModel.moveQueueItem(
                            index,
                            index + 1
                        )
                    }
                },
                onClear = {
                    viewModel.clearQueue()
                    showQueue = false
                }
            )
        }

        return
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "SHN Music",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge
                        )

                        Text(
                            text =
                                "$count songs",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick =
                            viewModel::refresh
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription =
                                "Refresh"
                        )
                    }
                }
            )
        },

        bottomBar = {

            if (
                player.title.isNotBlank()
            ) {

                MiniPlayer(
                    title = player.title,
                    artist = player.artist,
                    isPlaying =
                        player.isPlaying,
                    onClick = {
                        showNowPlaying = true
                    },
                    onPlayPause =
                        viewModel::togglePlayPause,
                    onNext =
                        viewModel::next,
                    onPrevious =
                        viewModel::previous,
                    onQueue = {
                        showQueue = true
                    }
                )
            }
        }
    ) { padding ->

        when {

            songs.isEmpty() -> {

                EmptyLibrary(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    onRefresh =
                        viewModel::refresh
                )
            }

            else -> {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                ) {

                    itemsIndexed(
                        items = songs,
                        key = { _, song ->
                            song.uri
                        }
                    ) { index, song ->

                        SongRow(
                            song = song,
                            onClick = {
                                viewModel.play(song)
                            },
                            onPlayNext = {
                                viewModel.playNext(song)
                            },
                            onAddQueue = {
                                viewModel.addToQueue(song)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showQueue) {

        QueueDialog(
            queue = queue,
            onDismiss = {
                showQueue = false
            },
            onSelect = {
                viewModel.selectQueueItem(it)
                showQueue = false
            },
            onRemove =
                viewModel::removeFromQueue,
            onMoveUp = { index ->

                if (index > 0) {
                    viewModel.moveQueueItem(
                        index,
                        index - 1
                    )
                }
            },
            onMoveDown = { index ->

                if (
                    index <
                    queue.lastIndex
                ) {
                    viewModel.moveQueueItem(
                        index,
                        index + 1
                    )
                }
            },
            onClear = {
                viewModel.clearQueue()
                showQueue = false
            }
        )
    }
}

@Composable
private fun SongRow(
    song: SongEntity,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddQueue: () -> Unit
) {

    var menuExpanded by
        remember {
            mutableStateOf(false)
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Artwork(
            uri = song.artworkUri,
            modifier =
                Modifier
                    .size(58.dp)
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    )
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = 14.dp
                    )
        ) {

            Text(
                text = song.title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                maxLines = 1
            )

            Text(
                text =
                    "${song.artist} â€¢ ${song.album}",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines = 1
            )
        }

        Box {

            IconButton(
                onClick = {
                    menuExpanded = true
                }
            ) {

                Icon(
                    Icons.Default.MoreVert,
                    contentDescription =
                        "More"
                )
            }

            DropdownMenu(
                expanded =
                    menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                }
            ) {

                DropdownMenuItem(
                    text = {
                        Text(
                            "Play next"
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onPlayNext()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            "Add to queue"
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onAddQueue()
                    }
                )
            }
        }
    }

    HorizontalDivider()
}

@Composable
private fun MiniPlayer(
    title: String,
    artist: String,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onQueue: () -> Unit
) {

    Surface(
        tonalElevation = 8.dp,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
    ) {

        Column {

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .padding(
                            horizontal = 10.dp
                        ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        text = title,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        maxLines = 1
                    )

                    Text(
                        text = artist,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onQueue
                ) {

                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription =
                            "Queue"
                    )
                }

                IconButton(
                    onClick = onPrevious
                ) {

                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription =
                            "Previous"
                    )
                }

                IconButton(
                    onClick = onPlayPause
                ) {

                    Icon(
                        imageVector =
                            if (isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                        contentDescription =
                            if (isPlaying) {
                                "Pause"
                            } else {
                                "Play"
                            }
                    )
                }

                IconButton(
                    onClick = onNext
                ) {

                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription =
                            "Next"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NowPlayingScreen(
    state: PlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onQueue: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Now Playing")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription =
                                "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal = 22.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Artwork(
                uri = state.artworkUri,
                modifier =
                    Modifier
                        .size(300.dp)
                        .clip(
                            RoundedCornerShape(
                                24.dp
                            )
                        )
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Text(
                text = state.title,
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                maxLines = 2
            )

            Text(
                text = state.artist,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines = 1
            )

            if (
                state.album.isNotBlank()
            ) {

                Text(
                    text = state.album,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Slider(
                value =
                    state.position
                        .coerceIn(
                            0L,
                            state.duration
                                .coerceAtLeast(
                                    1L
                                )
                        )
                        .toFloat(),

                onValueChange = {
                    onSeek(
                        it.toLong()
                    )
                },

                valueRange =
                    0f..state.duration
                        .coerceAtLeast(
                            1L
                        )
                        .toFloat(),

                modifier =
                    Modifier.fillMaxWidth()
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    formatTime(
                        state.position
                    )
                )

                Text(
                    formatTime(
                        state.duration
                    )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onShuffle
                ) {

                    Icon(
                        Icons.Default.Shuffle,
                        contentDescription =
                            "Shuffle",
                        tint =
                            if (
                                state.shuffleEnabled
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                            }
                    )
                }

                IconButton(
                    onClick = onPrevious
                ) {

                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription =
                            "Previous"
                    )
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier =
                        Modifier.size(74.dp)
                ) {

                    Icon(
                        imageVector =
                            if (state.isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                        contentDescription =
                            "Play/Pause",
                        modifier =
                            Modifier.size(48.dp)
                    )
                }

                IconButton(
                    onClick = onNext
                ) {

                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription =
                            "Next"
                    )
                }

                IconButton(
                    onClick = onRepeat
                ) {

                    Icon(
                        Icons.Default.Repeat,
                        contentDescription =
                            "Repeat",
                        tint =
                            if (
                                state.repeatMode !=
                                Player.REPEAT_MODE_OFF
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .primary
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                            }
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            TextButton(
                onClick = onQueue
            ) {

                Icon(
                    Icons.Default.QueueMusic,
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.size(8.dp)
                )

                Text("Queue")
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Icon(
                Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription =
                    null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QueueDialog(
    queue: List<QueueItemUi>,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onClear: () -> Unit
) {

    AlertDialog(

        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "Queue (${queue.size})"
            )
        },

        text = {

            if (queue.isEmpty()) {

                Text(
                    "Queue is empty"
                )

            } else {

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                420.dp
                            )
                ) {

                    itemsIndexed(
                        queue,
                        key = { _, item ->
                            item.uri
                        }
                    ) { _, item ->

                        Surface(
                            tonalElevation =
                                if (
                                    item.isCurrent
                                ) {
                                    5.dp
                                } else {
                                    0.dp
                                }
                        ) {

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSelect(
                                                item.index
                                            )
                                        }
                                        .padding(
                                            vertical = 7.dp
                                        ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        text =
                                            item.title,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .titleSmall,
                                        maxLines = 1
                                    )

                                    Text(
                                        text =
                                            item.artist,
                                        style =
                                            MaterialTheme
                                                .typography
                                                .bodySmall,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                IconButton(
                                    enabled =
                                        item.index > 0,
                                    onClick = {
                                        onMoveUp(
                                            item.index
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.KeyboardArrowUp,
                                        contentDescription =
                                            "Move up"
                                    )
                                }

                                IconButton(
                                    enabled =
                                        item.index <
                                            queue.lastIndex,
                                    onClick = {
                                        onMoveDown(
                                            item.index
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription =
                                            "Move down"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        onRemove(
                                            item.index
                                        )
                                    }
                                ) {

                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription =
                                            "Remove"
                                    )
                                }
                            }
                        }

                        HorizontalDivider()
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = onClear,
                enabled =
                    queue.isNotEmpty()
            ) {
                Text("Clear")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun Artwork(
    uri: String,
    modifier: Modifier
) {

    val context =
        LocalContext.current

    val bitmap by
        produceState<android.graphics.Bitmap?>(
            initialValue = null,
            key1 = uri
        ) {

            value =
                if (uri.isBlank()) {
                    null
                } else {

                    try {

                        context.contentResolver
                            .openInputStream(
                                Uri.parse(uri)
                            )
                            ?.use {
                                BitmapFactory
                                    .decodeStream(it)
                            }

                    } catch (
                        _: IOException
                    ) {
                        null

                    } catch (
                        _: SecurityException
                    ) {
                        null
                    }
                }
        }

    if (bitmap != null) {

        Image(
            bitmap =
                bitmap!!.asImageBitmap(),
            contentDescription =
                null,
            contentScale =
                ContentScale.Crop,
            modifier = modifier
        )

    } else {

        Box(
            modifier =
                modifier.background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = "SHN",
                style =
                    MaterialTheme
                        .typography
                        .headlineMedium
            )
        }
    }
}

private fun formatTime(
    millis: Long
): String {

    val total =
        (millis / 1000L)
            .coerceAtLeast(0L)

    val seconds =
        total % 60

    val minutes =
        (total / 60) % 60

    val hours =
        total / 3600

    return if (hours > 0) {

        "%d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )

    } else {

        "%02d:%02d".format(
            minutes,
            seconds
        )
    }
}

@Composable
private fun EmptyLibrary(
    modifier: Modifier,
    onRefresh: () -> Unit
) {

    Column(
        modifier = modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "No music found",
            style =
                MaterialTheme
                    .typography
                    .headlineSmall
        )

        Text(
            text =
                "Scan your device music library",
            modifier =
                Modifier.padding(
                    top = 8.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Button(
            onClick = onRefresh,
            modifier =
                Modifier.padding(
                    top = 20.dp
                )
        ) {
            Text("Scan Library")
        }
    }
}

