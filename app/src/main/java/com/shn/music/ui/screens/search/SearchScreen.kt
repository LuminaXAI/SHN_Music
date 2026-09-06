package com.shn.music.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.ui.screens.player.ArtworkImage
import com.shn.music.ui.strings.ShNStrings

@Composable
fun SearchRoute(query: String, repository: SongRepository, playerController: SHNMusicPlayerController, strings: ShNStrings = ShNStrings(com.shn.music.core.settings.AppLanguage.ENGLISH)) {
    val results by repository.search(query.trim()).collectAsStateWithLifecycle(initialValue = emptyList())
    if (query.isBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(strings.searchHint) }
        return
    }
    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SearchOff, null, Modifier.size(58.dp))
                Text(strings.noResults, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.titleLarge)
                Text(strings.tryAnother, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val groups = results.groupBy { it.artist.ifBlank { "Unknown artist" } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Text("${results.size} results", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp)) }
        groups.forEach { (artist, songs) ->
            item {
                Text(artist, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(songs, key = { it.uri }) { song ->
                SearchSongRow(song) { val idx = results.indexOfFirst { it.uri == song.uri }; playerController.playQueue(results, idx) }
            }
        }
    }
}

@Composable
private fun SearchSongRow(song: SongEntity, onPlay: () -> Unit) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onPlay).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtworkImage(song.artworkUri, Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
                Text(listOf(song.artist, song.album).filter { it.isNotBlank() }.joinToString(" â€¢ ").ifBlank { "Unknown" }, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onPlay) { Icon(Icons.Default.PlayArrow, "Play") }
        }
    }
}

