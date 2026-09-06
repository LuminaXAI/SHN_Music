package com.shn.music.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shn.music.core.settings.AppLanguage
import com.shn.music.core.settings.AppSettingsStore
import com.shn.music.core.settings.ThemeMode
import com.shn.music.ui.strings.ShNStrings

@Composable
fun SettingsRoute(store: AppSettingsStore, strings: ShNStrings) {
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = com.shn.music.core.settings.AppSettings())
    var showLanguage by remember { mutableStateOf(false) }
    var showTheme by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            SettingsHeader(strings.settings)
        }
        item {
            SettingRow(Icons.Default.Language, strings.languageLabel, if (settings.language == AppLanguage.ARABIC) strings.arabic else strings.english) { showLanguage = true }
        }
        item {
            SettingRow(Icons.Default.DarkMode, strings.appearance, when (settings.themeMode) { ThemeMode.SYSTEM -> strings.system; ThemeMode.LIGHT -> strings.light; ThemeMode.DARK -> strings.dark }) { showTheme = true }
        }
        item {
            SettingSwitchRow(Icons.Default.History, strings.savePosition, strings.savePositionDesc, settings.savePlaybackPosition) {
                scope.launch { store.setSavePlaybackPosition(it) }
            }
        }
        item { AboutCard(strings) }
    }

    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            title = { Text(strings.languageLabel) },
            text = {
                Column {
                    LanguageOption(strings.arabic, settings.language == AppLanguage.ARABIC) {
                        scope.launch { store.setLanguage(AppLanguage.ARABIC); showLanguage = false }
                    }
                    LanguageOption(strings.english, settings.language == AppLanguage.ENGLISH) {
                        scope.launch { store.setLanguage(AppLanguage.ENGLISH); showLanguage = false }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguage = false }) { Text(if (strings.isArabic) "ط¥ط؛ظ„ط§ظ‚" else "Close") } }
        )
    }

    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            title = { Text(strings.appearance) },
            text = {
                Column {
                    ThemeOption(strings.system, ThemeMode.SYSTEM == settings.themeMode) { scope.launch { store.setThemeMode(ThemeMode.SYSTEM); showTheme = false } }
                    ThemeOption(strings.light, ThemeMode.LIGHT == settings.themeMode) { scope.launch { store.setThemeMode(ThemeMode.LIGHT); showTheme = false } }
                    ThemeOption(strings.dark, ThemeMode.DARK == settings.themeMode) { scope.launch { store.setThemeMode(ThemeMode.DARK); showTheme = false } }
                }
            },
            confirmButton = { TextButton(onClick = { showTheme = false }) { Text(if (strings.isArabic) "ط¥ط؛ظ„ط§ظ‚" else "Close") } }
        )
    }
}

@Composable
private fun SettingsHeader(title: String) {
    Column(Modifier.fillMaxWidth().padding(18.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text("SHN Music", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(title); Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        Icon(Icons.Default.ChevronRight, null)
    }
    HorizontalDivider()
}

@Composable
private fun SettingSwitchRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(24.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) { Text(title); Text(desc, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
        Switch(checked, onCheckedChange)
    }
    HorizontalDivider()
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().selectable(selected = selected, enabled = true, role = Role.RadioButton, onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected, onClick = onClick); Text(label, Modifier.padding(start = 10.dp))
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) = LanguageOption(label, selected, onClick)

@Composable
private fun AboutCard(strings: ShNStrings) {
    ElevatedCard(Modifier.fillMaxWidth().padding(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Icon(Icons.Default.LibraryMusic, null, Modifier.size(30.dp))
            Text(strings.about, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            Text(strings.version, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


