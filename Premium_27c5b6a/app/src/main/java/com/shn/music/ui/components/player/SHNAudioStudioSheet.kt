@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.shn.music.ui.components.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shn.music.core.audio.AudioSettings
import com.shn.music.core.audio.AudioSettingsStore
import com.shn.music.core.media.player.SHNMusicPlayerController
import kotlinx.coroutines.launch

@Composable
fun SHNAudioStudioSheet(settingsStore: AudioSettingsStore, controller: SHNMusicPlayerController, onDismiss: () -> Unit) {
    val settings by settingsStore.settings.collectAsState(initial = AudioSettings())
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).navigationBarsPadding()) {
            Text("Audio Studio", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("Playback speed", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(.5f, .75f, 1f, 1.25f, 1.5f, 1.75f, 2f).forEach { speed ->
                    FilterChip(selected = settings.speed == speed, onClick = { controller.setPlaybackSpeed(speed); scope.launch { settingsStore.setSpeed(speed) } }, label = { Text("${speed}x") })
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Balance", style = MaterialTheme.typography.titleMedium)
            Slider(value = settings.balance, onValueChange = { v -> scope.launch { settingsStore.setBalance(v) } }, valueRange = -1f..1f)
            Text("Left  •  Center  •  Right", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text("Platform audio effects", style = MaterialTheme.typography.titleMedium)
            Text("Effects are applied only when the playback device supports them.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bass boost"); Switch(checked = settings.bassEnabled, onCheckedChange = { scope.launch { settingsStore.setBass(it, settings.bassStrength) } })
            }
            Slider(value = settings.bassStrength.toFloat(), onValueChange = { scope.launch { settingsStore.setBass(settings.bassEnabled, it.toInt()) } }, valueRange = 0f..1000f, enabled = settings.bassEnabled)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Virtualizer"); Switch(checked = settings.virtualizerEnabled, onCheckedChange = { scope.launch { settingsStore.setVirtualizer(it, settings.virtualizerStrength) } })
            }
            Slider(value = settings.virtualizerStrength.toFloat(), onValueChange = { scope.launch { settingsStore.setVirtualizer(settings.virtualizerEnabled, it.toInt()) } }, valueRange = 0f..1000f, enabled = settings.virtualizerEnabled)
            Spacer(Modifier.height(24.dp))
        }
    }
}
