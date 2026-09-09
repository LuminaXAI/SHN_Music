package com.shn.music.ui.components.player

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SHNMarqueeText(text: String, modifier: Modifier = Modifier, style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current) {
    Text(text = text, modifier = modifier.basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1100, repeatDelayMillis = 900, spacing = MarqueeSpacing(32.dp)), maxLines = 1, overflow = TextOverflow.Clip, style = style)
}
