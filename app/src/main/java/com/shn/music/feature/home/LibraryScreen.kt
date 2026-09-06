@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shn.music.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.shn.music.SHNMusicApplication
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.database.model.*
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.ui.screens.playlists.PlaylistsRoute
import com.shn.music.ui.screens.player.ArtworkImage
import com.shn.music.ui.strings.ShNStrings
import kotlinx.coroutines.launch

enum class LibrarySection(val title: String) { SONGS("Songs"), ALBUMS("Albums"), ARTISTS("Artists"), GENRES("Genres"), FOLDERS("Folders"), PLAYLISTS("Playlists") }

@Composable
fun LibraryRoute(initialSection: LibrarySection = LibrarySection.SONGS, repository: SongRepository, playerController: SHNMusicPlayerController, strings: ShNStrings = ShNStrings(com.shn.music.core.settings.AppLanguage.ENGLISH)) {
    val songs by repository.observeSongs().collectAsStateWithLifecycle(emptyList())
    val albums by repository.observeAlbums().collectAsStateWithLifecycle(emptyList())
    val artists by repository.observeArtists().collectAsStateWithLifecycle(emptyList())
    val genres by repository.observeGenres().collectAsStateWithLifecycle(emptyList())
    val folders by repository.observeFolders().collectAsStateWithLifecycle(emptyList())
    var section by remember(initialSection) { mutableStateOf(initialSection) }
    var detail by remember { mutableStateOf<Pair<String, List<SongEntity>>?>(null) }
    val scope = rememberCoroutineScope()

    if (section == LibrarySection.PLAYLISTS) {
        val app = LocalContext.current.applicationContext as SHNMusicApplication
        PlaylistsRoute(app.container.playlistRepository, repository, playerController)
        return
    }

    if (detail != null) {
        LibraryDetailRoute(detail!!.first, "${detail!!.second.size} ${strings.songs}", detail!!.second, repository, playerController) { detail = null }
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LibrarySection.entries.filter { it != LibrarySection.PLAYLISTS }.forEach { item ->
                FilterChip(selected = item == section, onClick = { section = item }, label = { Text(localizedSectionTitle(item, strings)) })
            }
        }
        when (section) {
            LibrarySection.SONGS -> SongsLibrary(songs, playerController)
            LibrarySection.ALBUMS -> AggregateLibrary(albums.map { Triple(it.name, it.artist, it.artworkUri) }, { it.second.isNotBlank() }) { name, artist, _ -> scope.launch { detail = name to repository.getSongsByAlbum(name, artist) } }
            LibrarySection.ARTISTS -> AggregateLibrary(artists.map { Triple(it.name, "", "") }, { false }) { name, _, _ -> scope.launch { detail = name to repository.getSongsByArtist(name) } }
            LibrarySection.GENRES -> AggregateLibrary(genres.map { Triple(it.name, "", "") }, { false }) { name, _, _ -> scope.launch { detail = name to repository.getSongsByGenre(name) } }
            LibrarySection.FOLDERS -> AggregateLibrary(folders.map { Triple(it.name, "", "") }, { false }) { name, _, _ -> scope.launch { detail = name to repository.getSongsByFolder(name) } }
            LibrarySection.PLAYLISTS -> Unit
        }
    }
}

@Composable private fun SongsLibrary(songs: List<SongEntity>, player: SHNMusicPlayerController) {
    if (songs.isEmpty()) { EmptyLibrary("No songs"); return }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) { items(songs, key = { it.uri }) { song ->
        Row(Modifier.fillMaxWidth().clickable { val i=songs.indexOfFirst { it.uri==song.uri }; player.playQueue(songs,i) }.padding(10.dp), verticalAlignment=Alignment.CenterVertically) {
            ArtworkImage(song.artworkUri, Modifier.size(56.dp)); Column(Modifier.weight(1f).padding(horizontal=12.dp)) { Text(song.title, maxLines=1); Text(song.artist, maxLines=1, color=MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    } }
}

@Composable private fun AggregateLibrary(items: List<Triple<String,String,String>>, hasArt: (Triple<String,String,String>)->Boolean, onOpen: (String,String,String)->Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding=PaddingValues(bottom=16.dp)) { items(items, key={ "${it.first}-${it.second}" }) { item ->
        Row(Modifier.fillMaxWidth().clickable { onOpen(item.first,item.second,item.third) }.padding(12.dp), verticalAlignment=Alignment.CenterVertically) {
            if (hasArt(item)) ArtworkImage(item.third, Modifier.size(60.dp)) else Icon(Icons.Default.LibraryMusic, null, Modifier.size(42.dp))
            Column(Modifier.weight(1f).padding(horizontal=12.dp)) { Text(item.first, maxLines=1); if(item.second.isNotBlank()) Text(item.second, maxLines=1, color=MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Default.ChevronRight, null)
        }
        HorizontalDivider()
    } }
}

@Composable private fun EmptyLibrary(title: String) { Box(Modifier.fillMaxSize(), contentAlignment=Alignment.Center) { Text(title) } }

private fun localizedSectionTitle(section: LibrarySection, strings: ShNStrings): String = when (section) {
    LibrarySection.SONGS -> strings.songs
    LibrarySection.ALBUMS -> strings.albums
    LibrarySection.ARTISTS -> strings.artists
    LibrarySection.GENRES -> strings.genres
    LibrarySection.FOLDERS -> strings.folders
    LibrarySection.PLAYLISTS -> strings.playlists
}
