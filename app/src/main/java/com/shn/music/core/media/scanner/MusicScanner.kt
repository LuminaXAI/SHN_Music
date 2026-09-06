package com.shn.music.core.media.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.shn.music.core.database.entity.SongEntity

class MusicScanner(
    context: Context
) {
    private val resolver: ContentResolver = context.contentResolver

    fun scan(): List<SongEntity> {

        val collection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.RELATIVE_PATH
        )

        val result = ArrayList<SongEntity>()

        resolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->

            val idIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media._ID)

            val titleIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)

            val displayNameIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

            val artistIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)

            val albumIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)

            val albumArtistIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)

            val albumIdIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            val composerIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER)

            val durationIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

            val trackIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)

            val yearIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)

            val dateAddedIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            val dateModifiedIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)

            val sizeIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

            val relativePathIndex =
                cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)

            while (cursor.moveToNext()) {

                if (idIndex < 0) continue

                val id = cursor.getLong(idIndex)

                val displayName =
                    readText(
                        cursor,
                        displayNameIndex,
                        "Unknown"
                    )

                val title =
                    readText(
                        cursor,
                        titleIndex,
                        displayName.substringBeforeLast(
                            '.',
                            missingDelimiterValue = displayName
                        )
                    )

                val artist =
                    readText(
                        cursor,
                        artistIndex,
                        "Unknown Artist"
                    )

                val album =
                    readText(
                        cursor,
                        albumIndex,
                        "Unknown Album"
                    )

                val albumArtist =
                    readText(
                        cursor,
                        albumArtistIndex,
                        artist
                    )

                val composer =
                    readText(
                        cursor,
                        composerIndex,
                        ""
                    )

                val duration =
                    readLong(
                        cursor,
                        durationIndex,
                        0L
                    )

                if (duration <= 0L) continue

                val track =
                    readInt(
                        cursor,
                        trackIndex,
                        0
                    )

                val year =
                    readInt(
                        cursor,
                        yearIndex,
                        0
                    )

                val dateAdded =
                    readLong(
                        cursor,
                        dateAddedIndex,
                        0L
                    )

                val dateModified =
                    readLong(
                        cursor,
                        dateModifiedIndex,
                        0L
                    )

                val size =
                    readLong(
                        cursor,
                        sizeIndex,
                        0L
                    )

                val relativePath =
                    readText(
                        cursor,
                        relativePathIndex,
                        ""
                    )

                val albumId =
                    readLong(
                        cursor,
                        albumIdIndex,
                        -1L
                    )

                val uri =
                    ContentUris
                        .withAppendedId(
                            collection,
                            id
                        )
                        .toString()

                val artworkUri =
                    if (albumId >= 0L) {
                        Uri.parse(
                            "content://media/external/audio/albumart/$albumId"
                        ).toString()
                    } else {
                        ""
                    }

                result += SongEntity(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    albumArtist = albumArtist,
                    genre = "",
                    composer = composer,
                    duration = duration,
                    trackNumber = track,
                    year = year,
                    uri = uri,
                    displayName = displayName,
                    relativePath = relativePath,
                    artworkUri = artworkUri,
                    dateAdded = dateAdded,
                    dateModified = dateModified,
                    size = size
                )
            }
        }

        return result
    }

    private fun readText(
        cursor: android.database.Cursor,
        index: Int,
        fallback: String
    ): String {
        if (index < 0 || cursor.isNull(index)) {
            return fallback
        }

        return cursor.getString(index)
            .trim()
            .ifBlank { fallback }
    }

    private fun readLong(
        cursor: android.database.Cursor,
        index: Int,
        fallback: Long
    ): Long {
        if (index < 0 || cursor.isNull(index)) {
            return fallback
        }

        return cursor.getLong(index)
    }

    private fun readInt(
        cursor: android.database.Cursor,
        index: Int,
        fallback: Int
    ): Int {
        if (index < 0 || cursor.isNull(index)) {
            return fallback
        }

        return cursor.getInt(index)
    }
}
