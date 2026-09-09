package com.shn.music.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.AlbumGroup
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.ui.screens.player.ArtworkImage
import com.shn.music.ui.components.player.PlaybackIndicator
import com.shn.music.ui.strings.ShNStrings
import kotlinx.coroutines.launch

@Composable
fun HomeRoute(
    repository: SongRepository,
    playerController: SHNMusicPlayerController,
    onOpenSongs: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenMostPlayed: () -> Unit,
    strings: ShNStrings = ShNStrings(com.shn.music.core.settings.AppLanguage.ENGLISH)
) {
    val songs by repository.observeSongs().collectAsStateWithLifecycle(emptyList())
    val recent by repository.observeRecentlyPlayed().collectAsStateWithLifecycle(emptyList())
    val favorites by repository.observeFavorites().collectAsStateWithLifecycle(emptyList())
    val mostPlayed by repository.observeMostPlayed().collectAsStateWithLifecycle(emptyList())
    val recentlyAdded by repository.observeRecentlyAdded().collectAsStateWithLifecycle(emptyList())
    val albums by repository.observeAlbums().collectAsStateWithLifecycle(emptyList())
    val player by playerController.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            HeroCard(
                strings = strings,
                title = player.title,
                artist = player.artist,
                album = player.album,
                artwork = player.artworkUri,
                playing = player.isPlaying,
                onPlayPause = playerController::togglePlayPause,
                onPrevious = playerController::previous,
                onNext = playerController::next
            )
        }
        item {
            SectionTitle(strings.library)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { QuickCard(strings.songs, Icons.Default.LibraryMusic, onOpenSongs) }
                item { QuickCard(strings.albums, Icons.Default.Album, onOpenAlbums) }
                item { QuickCard(strings.playlists, Icons.Default.QueueMusic, onOpenPlaylists) }
                item { QuickCard(strings.favorites, Icons.Default.Favorite, onOpenFavorites) }
                item { QuickCard(strings.recent, Icons.Default.History, onOpenRecent) }
            }
        }
        item { SongSection(strings, strings.recentlyPlayed, recent, onOpenRecent, playerController) }
        item { SongSection(strings, strings.recentlyAdded, recentlyAdded, onOpenSongs, playerController) }
        item { SongSection(strings, strings.favorites, favorites, onOpenFavorites, playerController) }
        item { AlbumSection(strings, strings.albums, albums, onOpenAlbums, playerController, repository) }
        item { SongSection(strings, strings.mostPlayed, mostPlayed, onOpenMostPlayed, playerController) }
        item {
            Surface(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .58f)
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat(Icons.Default.MusicNote, songs.size)
                    Stat(Icons.Default.Album, albums.size)
                    Stat(Icons.Default.Favorite, favorites.size)
                }
            }
        }
    }
}

@Composable private fun HeroCard(strings: ShNStrings, title: String, artist: String, album: String, artwork: String, playing: Boolean, onPlayPause: () -> Unit, onPrevious: () -> Unit, onNext: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            Modifier.fillMaxWidth().background(
                Brush.horizontalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f))
                )
            ).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ArtworkImage(artwork, Modifier.size(88.dp).clip(RoundedCornerShape(22.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (title.isNotBlank()) PlaybackIndicator(playing, Modifier.padding(end = 7.dp))
                    Text(
                        if (title.isBlank()) strings.ready.uppercase() else strings.nowPlaying.uppercase(),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(title.ifBlank { strings.chooseSong }, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                if (title.isNotBlank()) Text(artist.ifBlank { strings.unknownArtist }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (title.isNotBlank() && album.isNotBlank()) Text(album, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (title.isNotBlank()) {
                IconButton(onClick = onPrevious) { Icon(Icons.Default.SkipPrevious, null) }
                FilledIconButton(onClick = onPlayPause) { Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null) }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, null) }
            }
        }
    }
}

@Composable private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge)
}

@Composable private fun QuickCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        Modifier.size(width = 118.dp, height = 112.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .72f)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(icon, null, Modifier.padding(8.dp).size(22.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text(title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable private fun SongSection(strings: ShNStrings, title: String, songs: List<SongEntity>, onSeeAll: () -> Unit, player: SHNMusicPlayerController) {
    if (songs.isEmpty()) return
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); Text(strings.seeAll, Modifier.clickable(onClick = onSeeAll), color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(songs.take(12), key = { it.uri }) { song ->
            Surface(
                Modifier.width(154.dp).clickable { val idx = songs.indexOfFirst { it.uri == song.uri }; player.playQueue(songs, idx) },
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)
            ) {
                Column(Modifier.padding(10.dp)) {
                    ArtworkImage(song.artworkUri, Modifier.fillMaxWidth().height(128.dp).clip(RoundedCornerShape(17.dp)))
                    Spacer(Modifier.height(8.dp)); Text(song.title, maxLines = 1); Text(song.artist.ifBlank { strings.unknownArtist }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        } }
    }
}

@Composable private fun AlbumSection(strings: ShNStrings, title: String, albums: List<AlbumGroup>, onSeeAll: () -> Unit, player: SHNMusicPlayerController, repository: SongRepository) {
    if (albums.isEmpty()) return
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge); Text(strings.seeAll, Modifier.clickable(onClick = onSeeAll), color = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(albums.take(12), key = { "${it.name}-${it.artist}" }) { album ->
            Surface(Modifier.width(154.dp).clickable {
                kotlinx.coroutines.MainScope().launch {
                    val songs = repository.getSongsByAlbum(album.name, album.artist)
                    if (songs.isNotEmpty()) player.playQueue(songs, 0)
                }
            }, shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
                Column(Modifier.padding(10.dp)) {
                    ArtworkImage(album.artworkUri, Modifier.fillMaxWidth().height(128.dp).clip(RoundedCornerShape(17.dp)))
                    Spacer(Modifier.height(8.dp)); Text(album.name, maxLines = 1); Text(album.artist.ifBlank { "${album.songCount} songs" }, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        } }
    }
}

@Composable private fun Stat(icon: ImageVector, value: Int) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null); Text(value.toString(), style = MaterialTheme.typography.titleLarge) } }
