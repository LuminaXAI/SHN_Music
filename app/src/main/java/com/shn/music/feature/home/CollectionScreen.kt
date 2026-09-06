@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shn.music.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.shn.music.core.database.entity.SongEntity
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.feature.songs.SongsViewModel
import com.shn.music.feature.songs.SongsViewModelFactory

enum class CollectionType {
    FAVORITES,
    RECENT,
    MOST_PLAYED
}

@Composable
fun CollectionRoute(
    type: CollectionType,
    repository: SongRepository,
    playerController: SHNMusicPlayerController
) {

    val vm: SongsViewModel =
        viewModel(
            factory =
                SongsViewModelFactory(
                    repository = repository,
                    playerController =
                        playerController
                )
        )

    val songsState =
        when (type) {

            CollectionType.FAVORITES ->
                vm.favorites

            CollectionType.RECENT ->
                vm.recentlyPlayed

            CollectionType.MOST_PLAYED ->
                vm.mostPlayed
        }.collectAsStateWithLifecycle()

    val player by
        vm.playerState
            .collectAsStateWithLifecycle()

    val title =
        when (type) {

            CollectionType.FAVORITES ->
                "Favorites"

            CollectionType.RECENT ->
                "Recently Played"

            CollectionType.MOST_PLAYED ->
                "Most Played"
        }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(title)
                }
            )
        },

        bottomBar = {

            if (player.title.isNotBlank()) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .surfaceContainer
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 7.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text = player.title,
                            maxLines = 1,
                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall
                        )

                        Text(
                            text = player.artist,
                            maxLines = 1,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall
                        )
                    }

                    IconButton(
                        onClick =
                            vm::togglePlayPause
                    ) {

                        Icon(
                            imageVector =
                                if (player.isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                            contentDescription =
                                "Play/Pause"
                        )
                    }
                }
            }
        }
    ) { padding ->

        val songs =
            songsState.value

        if (songs.isEmpty()) {

            EmptyCollection(
                type = type,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
            )

        } else {

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

                    CollectionSongRow(
                        song = song,
                        rank = index + 1,
                        onClick = {
                            vm.playList(
                                songs,
                                index
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CollectionSongRow(
    song: SongEntity,
    rank: Int,
    onClick: () -> Unit
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = rank.toString(),
            modifier =
                Modifier.size(34.dp),
            style =
                MaterialTheme
                    .typography
                    .labelLarge
        )

        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                    ),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    Icons.Default.MusicNote,
                contentDescription = null
            )
        }

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp
                    )
        ) {

            Text(
                text = song.title,
                maxLines = 1,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )

            Text(
                text =
                    "${song.artist} • ${song.album}",
                maxLines = 1,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )
        }
    }
}

@Composable
private fun EmptyCollection(
    type: CollectionType,
    modifier: Modifier
) {

    val icon: ImageVector =
        when (type) {

            CollectionType.FAVORITES ->
                Icons.Default.Favorite

            CollectionType.RECENT ->
                Icons.Default.History

            CollectionType.MOST_PLAYED ->
                Icons.Default.GraphicEq
        }

    val message =
        when (type) {

            CollectionType.FAVORITES ->
                "No favorites yet"

            CollectionType.RECENT ->
                "No recently played songs"

            CollectionType.MOST_PLAYED ->
                "No play statistics yet"
        }

    Column(
        modifier = modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(52.dp)
        )

        Text(
            text = message,
            modifier =
                Modifier.padding(
                    top = 12.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .titleMedium
        )
    }
}
