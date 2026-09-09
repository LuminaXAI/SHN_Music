package com.shn.music.ui.components.player

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun SHNPlayerGestureContainer(onNext: () -> Unit, onPrevious: () -> Unit, content: @Composable (Modifier) -> Unit) {
    var x by remember { mutableFloatStateOf(0f) }
    content(Modifier.offset { IntOffset(x.roundToInt(), 0) }.pointerInput(Unit) {
        detectHorizontalDragGestures(onHorizontalDrag = { change, amount -> change.consume(); x += amount }, onDragEnd = { if (x < -120f) onNext() else if (x > 120f) onPrevious(); x = 0f }, onDragCancel = { x = 0f })
    })
}
