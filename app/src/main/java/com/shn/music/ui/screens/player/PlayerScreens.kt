@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shn.music.ui.screens.player

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import com.shn.music.core.media.player.PlayerUiState
import com.shn.music.core.media.player.QueueItemUi
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.ui.strings.ShNStrings
import com.shn.music.core.audio.AudioSettingsStore
import com.shn.music.ui.components.player.SHNMarqueeText
import com.shn.music.ui.components.player.SHNPlayerGestureContainer
import com.shn.music.ui.components.player.SHNAudioStudioSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

@Composable
fun ArtworkImage(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(null, uri) {
        value = if (uri.isBlank()) null else withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
    if (bitmap != null) {
        Image(bitmap!!.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = modifier)
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(38.dp)) }
    }
}

@Composable
fun SHNMiniPlayer(
    state: PlayerUiState,
    queueSize: Int,
    onClick: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onQueue: () -> Unit
) {
    Surface(Modifier.fillMaxWidth(), tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(
            Modifier.fillMaxWidth().height(72.dp).clickable(onClick = onClick).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkImage(state.artworkUri, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                SHNMarqueeText(state.title, style = MaterialTheme.typography.titleSmall)
                Text(state.artist.ifBlank { "Unknown artist" }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (queueSize > 0) IconButton(onClick = onQueue) { Icon(Icons.AutoMirrored.Filled.QueueMusic, "Queue") }
            IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, "Previous") }
            IconButton(onClick = onPlayPause) { Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play/Pause") }
            IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Next") }
        }
    }
}

@Composable
fun SHNNowPlayingRoute(
    repository: SongRepository,
    playerController: SHNMusicPlayerController,
    audioSettingsStore: AudioSettingsStore,
    strings: ShNStrings,
    onBack: () -> Unit,
    onQueue: () -> Unit
) {
    val state by playerController.state.collectAsStateWithLifecycle()
    val queue by playerController.queue.collectAsStateWithLifecycle()
    var favorite by remember(state.uri) { mutableStateOf(false) }
    var sliderPosition by remember(state.uri) { mutableFloatStateOf(state.position.toFloat()) }
    var showAudioStudio by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.uri) {
        sliderPosition = state.position.toFloat()
        favorite = state.uri.isNotBlank() && repository.getSongByUri(state.uri)?.isFavorite == true
    }
    LaunchedEffect(state.position, state.uri) {
        sliderPosition = state.position.toFloat()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(strings.nowPlaying) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = { IconButton(onClick = onQueue) { Icon(Icons.AutoMirrored.Filled.QueueMusic, strings.queue) } }
            )
        }
    ) { padding ->
        if (state.title.isBlank()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(strings.nothingPlaying) }
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            SHNPlayerGestureContainer(onNext = playerController::next, onPrevious = playerController::previous) { gestureModifier ->
                ArtworkImage(state.artworkUri, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(28.dp)).then(gestureModifier))
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    SHNMarqueeText(state.title, style = MaterialTheme.typography.headlineSmall)
                    Text(state.artist.ifBlank { strings.unknownArtist }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (state.album.isNotBlank()) Text(state.album, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = {
                    val uri = state.uri
                    if (uri.isNotBlank()) scope.launch(Dispatchers.Main) {
                        val song = repository.getSongByUri(uri)
                        if (song != null) {
                            val next = !song.isFavorite
                            repository.setFavorite(song.id, next)
                            favorite = next
                        }
                    }
                }) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null) }
            }
            Spacer(Modifier.height(14.dp))
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                onValueChangeFinished = { playerController.seekTo(sliderPosition.toLong()) },
                valueRange = 0f..state.duration.coerceAtLeast(1L).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(state.position)); Text(formatTime(state.duration))
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = playerController::toggleShuffle) { Icon(Icons.Default.Shuffle, null, tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current) }
                IconButton(onClick = playerController::previous) { Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(34.dp)) }
                FilledIconButton(onClick = playerController::togglePlayPause, modifier = Modifier.size(76.dp)) { Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, modifier = Modifier.size(38.dp)) }
                IconButton(onClick = playerController::next) { Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(34.dp)) }
                IconButton(onClick = playerController::cycleRepeatMode) { Icon(Icons.Default.Repeat, null, tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else LocalContentColor.current) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showAudioStudio = true }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Tune, null); Spacer(Modifier.width(6.dp)); Text("Audio Studio") }
                OutlinedButton(onClick = { if (state.abStartMs == null) playerController.setAbStart() else if (state.abEndMs == null) playerController.setAbEnd() else playerController.clearAbLoop() }, modifier = Modifier.weight(1f)) { Text(if (state.abEndMs != null) "A-B on" else "Set A-B") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { playerController.startSleepTimer(15) }) { Text("Sleep 15m") }
                TextButton(onClick = playerController::sleepAtEndOfSong) { Text("End of song") }
                if (state.sleepRemainingMs != null) TextButton(onClick = playerController::cancelSleepTimer) { Text("Cancel sleep") }
            }
            OutlinedButton(onClick = onQueue, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.QueueMusic, null); Spacer(Modifier.width(8.dp)); Text("${strings.queue} • ${queue.size}")
            }
        }
    }
    if (showAudioStudio) SHNAudioStudioSheet(audioSettingsStore, playerController) { showAudioStudio = false }
}

@Composable
fun SHNQueueSheet(
    queue: List<QueueItemUi>,
    strings: ShNStrings,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onMove: (Int, Int) -> Unit,
    onClear: () -> Unit
) {
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragDelta by remember { mutableFloatStateOf(0f) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(strings.queue, style = MaterialTheme.typography.headlineSmall); Text("${queue.size}") }
            TextButton(onClick = onClear, enabled = queue.isNotEmpty()) { Text(strings.clear) }
        }
        HorizontalDivider()
        if (queue.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(260.dp), contentAlignment = Alignment.Center) { Text(strings.emptyQueue) }
        } else {
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 560.dp)) {
                items(queue, key = { it.uri }) { item ->
                    val index = queue.indexOfFirst { it.uri == item.uri }
                    Row(
                        Modifier.fillMaxWidth().background(if (item.isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface).clickable { onSelect(index) }.padding(horizontal = 14.dp, vertical = 9.dp).pointerInput(item.uri) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { dragIndex = index; dragDelta = 0f },
                                onDragCancel = { dragIndex = -1; dragDelta = 0f },
                                onDragEnd = { dragIndex = -1; dragDelta = 0f },
                                onDrag = { change, amount ->
                                    change.consume(); if (dragIndex != index) return@detectDragGesturesAfterLongPress
                                    dragDelta += amount.y
                                    val threshold = 58f
                                    if (dragDelta > threshold && index < queue.lastIndex) { onMove(index, index + 1); dragDelta = 0f }
                                    else if (dragDelta < -threshold && index > 0) { onMove(index, index - 1); dragDelta = 0f }
                                }
                            )
                        }, verticalAlignment = Alignment.CenterVertically
                    ) {
                        ArtworkImage(item.artworkUri, Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)))
                        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                            Text(item.title, maxLines = 1); Text(item.artist.ifBlank { strings.unknownArtist }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.isCurrent) Icon(Icons.Default.Equalizer, null, tint = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { onRemove(index) }) { Icon(Icons.Default.DeleteOutline, null) }
                    }
                    HorizontalDivider()
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

private fun formatTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000L); val minutes = total / 60L; val seconds = total % 60L
    return if (minutes >= 60) "%d:%02d:%02d".format(minutes / 60, minutes % 60, seconds) else "%d:%02d".format(minutes, seconds)
}
