package com.shn.music.core.lyrics

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LyricLine(val timeMs: Long?, val text: String)
data class Lyrics(val lines: List<LyricLine>) { val synchronized get() = lines.any { it.timeMs != null } }
interface LyricsProvider { suspend fun lyricsFor(audioUri: String): Lyrics? }

/** Looks only for a sidecar .lrc file next to a file URI. No network and no invented lyrics. */
class LocalLyricsProvider(private val context: Context) : LyricsProvider {
    override suspend fun lyricsFor(audioUri: String): Lyrics? = withContext(Dispatchers.IO) {
        val uri = Uri.parse(audioUri)
        if (uri.scheme != "file") return@withContext null
        val path = uri.path ?: return@withContext null
        val file = java.io.File(path.substringBeforeLast('.') + ".lrc")
        if (!file.isFile) return@withContext null
        file.readLines().mapNotNull { raw ->
            val match = Regex("\\\\[(\\\\d{1,2}):(\\\\d{2})(?:\\\\.(\\\\d{1,3}))?\\\\](.*)").matchEntire(raw)
            if (match == null) LyricLine(null, raw.takeIf { it.isNotBlank() } ?: return@mapNotNull null)
            else LyricLine((match.groupValues[1].toLong() * 60_000) + (match.groupValues[2].toLong() * 1_000) + match.groupValues[3].padEnd(3, '0').take(3).toLong(), match.groupValues[4].trim()).takeIf { it.text.isNotBlank() }
        }.takeIf { it.isNotEmpty() }?.let(::Lyrics)
    }
}
