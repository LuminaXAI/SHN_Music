@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.shn.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shn.music.core.settings.AppLanguage
import com.shn.music.core.settings.AppSettingsStore
import com.shn.music.core.media.player.SHNMusicPlayerController
import com.shn.music.data.repository.SongRepository
import com.shn.music.feature.home.CollectionRoute
import com.shn.music.feature.home.CollectionType
import com.shn.music.feature.home.HomeRoute
import com.shn.music.feature.home.LibraryRoute
import com.shn.music.feature.home.LibrarySection
import com.shn.music.ui.screens.player.SHNMiniPlayer
import com.shn.music.ui.screens.player.SHNNowPlayingRoute
import com.shn.music.ui.screens.player.SHNQueueSheet
import com.shn.music.ui.screens.search.SearchRoute
import com.shn.music.ui.screens.settings.SettingsRoute
import com.shn.music.ui.strings.ShNStrings
import com.shn.music.ui.theme.SHNMusicTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var permissionState by mutableIntStateOf(0)
    private lateinit var settingsStore: AppSettingsStore

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissionState++
            render()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsStore = AppSettingsStore(this)
        permissionState = if (hasPermission(audioPermission())) 1 else 0
        render()
    }

    private fun render() {
        setContent { RootContent() }
    }

    @Composable
    private fun RootContent() {
        val settings by settingsStore.settings.collectAsStateWithLifecycle(
            initialValue = com.shn.music.core.settings.AppSettings()
        )
        val strings = remember(settings.language) { ShNStrings(settings.language) }
        val direction = if (strings.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

        androidx.compose.runtime.CompositionLocalProvider(
            LocalLayoutDirection provides direction
        ) {
            SHNMusicTheme(
                themeMode = when (settings.themeMode) {
                    com.shn.music.core.settings.ThemeMode.SYSTEM -> com.shn.music.ui.theme.ThemeMode.SYSTEM
                    com.shn.music.core.settings.ThemeMode.LIGHT -> com.shn.music.ui.theme.ThemeMode.LIGHT
                    com.shn.music.core.settings.ThemeMode.DARK -> com.shn.music.ui.theme.ThemeMode.DARK
                }
            ) {
                if (permissionState == 0) {
                    PermissionScreen(
                        title = strings.appName,
                        onRequestPermission = ::requestPermissions
                    )
                } else {
                    val app = application as SHNMusicApplication
                    SHNMusicRoot(
                        repository = app.container.songRepository,
                        playerController = app.container.playerController,
                        settingsStore = settingsStore,
                        strings = strings
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(audioPermission())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    private fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

private data class SHNTab(
    val title: String,
    val section: LibrarySection? = null
)

@Composable
private fun SHNMusicRoot(
    repository: SongRepository,
    playerController: SHNMusicPlayerController,
    settingsStore: AppSettingsStore,
    strings: ShNStrings
) {
    val tabs = remember(strings.language) {
        listOf(
            SHNTab(strings.home),
            SHNTab(strings.songs, LibrarySection.SONGS),
            SHNTab(strings.albums, LibrarySection.ALBUMS),
            SHNTab(strings.artists, LibrarySection.ARTISTS),
            SHNTab(strings.genres, LibrarySection.GENRES),
            SHNTab(strings.folders, LibrarySection.FOLDERS),
            SHNTab(strings.playlists, LibrarySection.PLAYLISTS),
            SHNTab(strings.favorites),
            SHNTab(strings.recent),
            SHNTab(strings.mostPlayed)
        )
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val player by playerController.state.collectAsStateWithLifecycle()
    val queue by playerController.queue.collectAsStateWithLifecycle()
    val librarySongs by repository.observeSongs().collectAsStateWithLifecycle(emptyList())

    LaunchedEffect(librarySongs) {
        if (librarySongs.isNotEmpty()) {
            playerController.restoreQueue(librarySongs)
        }
    }

    BackHandler(enabled = showQueue) { showQueue = false }
    BackHandler(enabled = showNowPlaying && !showQueue) { showNowPlaying = false }
    BackHandler(enabled = showSearch && !showNowPlaying && !showQueue) {
        showSearch = false
        searchQuery = ""
    }
    BackHandler(enabled = showSettings && !showSearch && !showNowPlaying && !showQueue) {
        showSettings = false
    }

    Scaffold(
        topBar = {
            when {
                showSearch -> SearchTopBar(strings, searchQuery, { searchQuery = it }) {
                    showSearch = false
                    searchQuery = ""
                }
                showSettings -> SettingsTopBar(strings) { showSettings = false }
                showNowPlaying -> Unit
                else -> TopBar(
                    strings = strings,
                    menuExpanded = menuExpanded,
                    onSearch = { showSearch = true },
                    onMore = { menuExpanded = true },
                    onDismissMenu = { menuExpanded = false },
                    onQueue = { menuExpanded = false; showQueue = true },
                    onRefresh = {
                        menuExpanded = false
                        kotlinx.coroutines.MainScope().launch { repository.refresh() }
                    },
                    onSettings = { menuExpanded = false; showSettings = true }
                )
            }
        },
        bottomBar = {
            if (!showSearch && !showSettings && !showNowPlaying && player.title.isNotBlank()) {
                SHNMiniPlayer(
                    state = player,
                    queueSize = queue.size,
                    onClick = { showNowPlaying = true },
                    onPlayPause = playerController::togglePlayPause,
                    onNext = playerController::next,
                    onPrevious = playerController::previous,
                    onQueue = { showQueue = true }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                showSearch -> SearchRoute(searchQuery, repository, playerController, strings)
                showSettings -> SettingsRoute(settingsStore, strings)
                showNowPlaying -> SHNNowPlayingRoute(repository, playerController, strings, onBack = { showNowPlaying = false }, onQueue = { showQueue = true })
                else -> Column(Modifier.fillMaxSize()) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        edgePadding = 12.dp,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(tab.title, maxLines = 1) }
                            )
                        }
                    }

                    when (selectedTabIndex) {
                        0 -> HomeRoute(
                            repository = repository,
                            playerController = playerController,
                            onOpenSongs = { selectedTabIndex = 1 },
                            onOpenAlbums = { selectedTabIndex = 2 },
                            onOpenPlaylists = { selectedTabIndex = 6 },
                            onOpenFavorites = { selectedTabIndex = 7 },
                            onOpenRecent = { selectedTabIndex = 8 },
                            onOpenMostPlayed = { selectedTabIndex = 9 },
                            strings = strings
                        )
                        7 -> CollectionRoute(CollectionType.FAVORITES, repository, playerController)
                        8 -> CollectionRoute(CollectionType.RECENT, repository, playerController)
                        9 -> CollectionRoute(CollectionType.MOST_PLAYED, repository, playerController)
                        else -> LibraryRoute(
                            initialSection = tabs[selectedTabIndex].section ?: LibrarySection.SONGS,
                            repository = repository,
                            playerController = playerController,
                            strings = strings
                        )
                    }
                }
            }
        }
    }

    if (showQueue) {
        SHNQueueSheet(
            queue = queue,
            strings = strings,
            onDismiss = { showQueue = false },
            onSelect = { index -> playerController.selectQueueItem(index); showQueue = false; showNowPlaying = true },
            onRemove = playerController::removeFromQueue,
            onMove = playerController::moveQueueItem,
            onClear = playerController::clearQueue
        )
    }
}

@Composable
private fun TopBar(
    strings: ShNStrings,
    menuExpanded: Boolean,
    onSearch: () -> Unit,
    onMore: () -> Unit,
    onDismissMenu: () -> Unit,
    onQueue: () -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    TopAppBar(
        title = { Text(strings.appName, style = MaterialTheme.typography.titleLarge) },
        actions = {
            IconButton(onClick = onSearch) { Icon(Icons.Default.Search, strings.search) }
            IconButton(onClick = onMore) { Icon(Icons.Default.MoreVert, null) }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissMenu) {
                DropdownMenuItem(
                    text = { Text(strings.queue) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, null) },
                    onClick = onQueue
                )
                DropdownMenuItem(
                    text = { Text(strings.refresh) },
                    leadingIcon = { Icon(Icons.Default.Refresh, null) },
                    onClick = onRefresh
                )
                DropdownMenuItem(
                    text = { Text(strings.settings) },
                    onClick = onSettings
                )
            }
        }
    )
}

@Composable
private fun SearchTopBar(strings: ShNStrings, query: String, onQueryChange: (String) -> Unit, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(strings.searchHint) }
            )
        }
    )
}

@Composable
private fun SettingsTopBar(strings: ShNStrings, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
        title = { Text(strings.settings) }
    )
}

@Composable
private fun PermissionScreen(title: String, onRequestPermission: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text("Allow access to your music library", modifier = Modifier.padding(top = 8.dp))
        Button(onClick = onRequestPermission, modifier = Modifier.padding(top = 20.dp)) { Text("Allow") }
    }
}
