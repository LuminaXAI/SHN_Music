package com.shn.music.core.media.queue

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.shnQueueDataStore by preferencesDataStore(
    name = "shn_queue"
)

data class QueueSnapshot(
    val uris: List<String>,
    val currentIndex: Int,
    val position: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: Int,
    val playWhenReady: Boolean
)

class QueueStore(
    context: Context
) {
    private val appContext = context.applicationContext

    private object Keys {
        val uris = stringPreferencesKey("queue_uris")
        val currentIndex = intPreferencesKey("current_index")
        val position = longPreferencesKey("position")
        val shuffle = booleanPreferencesKey("shuffle")
        val repeat = intPreferencesKey("repeat")
        val playWhenReady = booleanPreferencesKey("play_when_ready")
    }

    suspend fun save(snapshot: QueueSnapshot) {
        appContext.shnQueueDataStore.edit { prefs ->
            prefs[Keys.uris] =
                snapshot.uris.joinToString("\u001F")

            prefs[Keys.currentIndex] =
                snapshot.currentIndex

            prefs[Keys.position] =
                snapshot.position

            prefs[Keys.shuffle] =
                snapshot.shuffleEnabled

            prefs[Keys.repeat] =
                snapshot.repeatMode

            prefs[Keys.playWhenReady] =
                snapshot.playWhenReady
        }
    }

    suspend fun load(): QueueSnapshot {
        val prefs =
            appContext.shnQueueDataStore.data.first()

        val encoded =
            prefs[Keys.uris].orEmpty()

        val uris =
            if (encoded.isBlank()) {
                emptyList()
            } else {
                encoded.split("\u001F")
                    .filter(String::isNotBlank)
            }

        return QueueSnapshot(
            uris = uris,
            currentIndex = prefs[Keys.currentIndex] ?: 0,
            position = prefs[Keys.position] ?: 0L,
            shuffleEnabled = prefs[Keys.shuffle] ?: false,
            repeatMode =
                prefs[Keys.repeat]
                    ?: androidx.media3.common.Player.REPEAT_MODE_OFF,
            playWhenReady = prefs[Keys.playWhenReady] ?: false
        )
    }

    suspend fun clear() {
        appContext.shnQueueDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
