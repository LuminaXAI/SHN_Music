@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shn.music.ui.screens.playlists

import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.PlaylistRepository
import com.shn.music.data.repository.SongRepository
import com.shn.music.feature.playlists.PlaylistsViewModel
import com.shn.music.feature.playlists.PlaylistsViewModelFactory

@Composable
fun PlaylistsRoute(
    playlistRepository: PlaylistRepository,
    songRepository: SongRepository,
    playerController: SHNMusicPlayerController
) {
    val vm: PlaylistsViewModel = viewModel(
        factory = PlaylistsViewModelFactory(
            repository = playlistRepository,
            playerController = playerController
        )
    )

    val playlists by vm.playlists.collectAsStateWithLifecycle()

    val allSongs by songRepository
        .observeSongs()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    var showCreate by remember { mutableStateOf(false) }
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var selectedPlaylistName by remember { mutableStateOf("") }
    var selectedSongs by remember { mutableStateOf<List<SongEntity>>(emptyList()) }

    var showAddSongs by remember { mutableStateOf(false) }
    var showRename by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPlaylistId) {
        val id = selectedPlaylistId

        if (id != null) {
            vm.loadSongs(id) {
                selectedSongs = it
            }
        } else {
            selectedSongs = emptyList()
        }
    }

    if (selectedPlaylistId != null) {

        PlaylistDetail(
            title = selectedPlaylistName,
            songs = selectedSongs,
            onBack = {
                selectedPlaylistId = null
                selectedPlaylistName = ""
                selectedSongs = emptyList()
            },
            onPlayAll = {
                vm.playPlaylist(selectedSongs)
            },
            onAddSongs = {
                showAddSongs = true
            },
            onRemoveSong = { song ->
                val id = selectedPlaylistId ?: return@PlaylistDetail

                vm.removeSong(
                    playlistId = id,
                    songId = song.id
                )

                vm.loadSongs(id) {
                    selectedSongs = it
                }
            },
            onRename = {
                showRename = true
            },
            onDelete = {
                showDelete = true
            }
        )

        if (showAddSongs) {
            AddSongsDialog(
                songs = allSongs,
                existing = selectedSongs,
                onDismiss = {
                    showAddSongs = false
                },
                onAdd = { song ->
                    val id = selectedPlaylistId
                        ?: return@AddSongsDialog

                    vm.addSong(
                        playlistId = id,
                        song = song
                    )

                    vm.loadSongs(id) {
                        selectedSongs = it
                    }
                }
            )
        }

        if (showRename) {
            RenamePlaylistDialog(
                currentName = selectedPlaylistName,
                onDismiss = {
                    showRename = false
                },
                onRename = { newName ->
                    val id = selectedPlaylistId
                        ?: return@RenamePlaylistDialog

                    vm.renamePlaylist(
                        playlistId = id,
                        name = newName
                    )

                    selectedPlaylistName = newName.trim()
                    showRename = false
                }
            )
        }

        if (showDelete) {
            AlertDialog(
                onDismissRequest = {
                    showDelete = false
                },
                title = {
                    Text("Delete playlist?")
                },
                text = {
                    Text(
                        "This removes the playlist and its song references."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = selectedPlaylistId
                                ?: return@TextButton

                            vm.deletePlaylist(id)

                            selectedPlaylistId = null
                            selectedPlaylistName = ""
                            selectedSongs = emptyList()
                            showDelete = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDelete = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Playlists")
                },
                actions = {
                    IconButton(
                        onClick = {
                            showCreate = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create playlist"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        if (playlists.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(68.dp)
                )

                Text(
                    text = "No playlists yet",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 14.dp)
                )

                Text(
                    text = "Create your first playlist.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Button(
                    onClick = {
                        showCreate = true
                    },
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Text(
                        text = "Create playlist",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(
                    items = playlists,
                    key = { it.id }
                ) { playlist ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedPlaylistId = playlist.id
                                selectedPlaylistName = playlist.name
                            }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 14.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp)
                        ) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "${playlist.songCount} songs",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        CreatePlaylistDialog(
            onDismiss = {
                showCreate = false
            },
            onCreate = { name ->
                vm.createPlaylist(name)
                showCreate = false
            }
        )
    }
}

@Composable
private fun PlaylistDetail(
    title: String,
    songs: List<SongEntity>,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onAddSongs: () -> Unit,
    onRemoveSong: (SongEntity) -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = title,
                            maxLines = 1
                        )

                        Text(
                            text = "${songs.size} songs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            text = "‹",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRename) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename"
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Button(
                    onClick = onPlayAll,
                    enabled = songs.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null
                    )

                    Text(
                        text = "Play all",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                OutlinedButton(
                    onClick = onAddSongs
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Text(
                        text = "Add songs",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            if (songs.isEmpty()) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "This playlist is empty",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = songs,
                        key = { it.id }
                    ) { song ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 10.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = song.title,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "${song.artist} • ${song.album}",
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            IconButton(
                                onClick = {
                                    onRemoveSong(song)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New playlist")
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                singleLine = true,
                label = {
                    Text("Playlist name")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(name)
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember(currentName) {
        mutableStateOf(currentName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rename playlist")
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                },
                singleLine = true,
                label = {
                    Text("Playlist name")
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onRename(name)
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddSongsDialog(
    songs: List<SongEntity>,
    existing: List<SongEntity>,
    onDismiss: () -> Unit,
    onAdd: (SongEntity) -> Unit
) {
    var query by remember {
        mutableStateOf("")
    }

    val existingIds = remember(existing) {
        existing.mapTo(hashSetOf()) {
            it.id
        }
    }

    val filtered = remember(
        songs,
        query,
        existingIds
    ) {
        val clean = query.trim()

        songs
            .asSequence()
            .filter {
                it.id !in existingIds
            }
            .filter {
                clean.isEmpty() ||
                    it.title.contains(clean, ignoreCase = true) ||
                    it.artist.contains(clean, ignoreCase = true) ||
                    it.album.contains(clean, ignoreCase = true)
            }
            .take(100)
            .toList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add songs")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Search")
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    items(
                        items = filtered,
                        key = { it.id }
                    ) { song ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAdd(song)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = song.title,
                                    maxLines = 1
                                )

                                Text(
                                    text = song.artist,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}



