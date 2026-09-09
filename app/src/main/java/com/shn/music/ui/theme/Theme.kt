package com.shn.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun SHNMusicTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val scheme = if (dark) {
        darkColorScheme(
            primary = VioletLight,
            onPrimary = Ink,
            primaryContainer = ColorPrimaryContainer,
            onPrimaryContainer = TextPrimary,
            secondary = Aqua,
            onSecondary = Ink,
            background = Ink,
            onBackground = TextPrimary,
            surface = SurfaceInk,
            onSurface = TextPrimary,
            surfaceVariant = SurfaceRaised,
            onSurfaceVariant = TextSecondary,
            outline = Color(0xFF777785)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6256B8),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE7E0FF),
            onPrimaryContainer = Color(0xFF21164F),
            secondary = Color(0xFF00695F),
            background = Paper,
            onBackground = InkLight,
            surface = Paper,
            onSurface = InkLight,
            surfaceVariant = PaperSurface
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(),
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(36.dp)
        ),
        content = content
    )
}

private val ColorPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF332B65)
