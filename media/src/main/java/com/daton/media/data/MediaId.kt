package com.daton.media.data

import android.os.Environment
import com.daton.media.playback.Loop
import com.daton.media.playback.Playlist
import com.daton.media.playback.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class MediaId(val source: String, val underlyingMediaId: MediaId? = null) {

    companion object {
        const val SONG_SOURCE_SHARED_STORAGE = "*song-shared-storage"
        const val LOOP_SOURCE = "*loop*"
        const val PLAYLIST_SOURCE = "*playlist*"

        fun deserialize(value: String): MediaId = Json.decodeFromString(value)

        fun deserializeIfValid(value: String): MediaId? =
            try {
                Json.decodeFromString(value)
            } catch (e: Exception) {
                null
            }

        // Stored for performance reasons
        val EXTERNAL_STORAGE_DIRECTORY: String =
            Environment.getExternalStorageDirectory().absolutePath
    }

    constructor(song: Song) : this(
        SONG_SOURCE_SHARED_STORAGE +
                song.path.absolutePath.substring(
                    song.path.absolutePath.indexOf(
                        EXTERNAL_STORAGE_DIRECTORY
                    ) + EXTERNAL_STORAGE_DIRECTORY.length
                )
    )

    constructor(loop: Loop) : this(LOOP_SOURCE + loop.name, loop.song.mediaId)

    constructor(playlist: Playlist) : this(PLAYLIST_SOURCE + playlist.name)

    val isSong: Boolean
        get() = source.contains(SONG_SOURCE_SHARED_STORAGE)

    val isLoop: Boolean
        get() = source.contains(LOOP_SOURCE) && underlyingMediaId != null

    val isPlaylist: Boolean
        get() = source.contains(PLAYLIST_SOURCE)

    val path: File
        get() {
            assert(!isPlaylist || underlyingMediaId != null) { "Cannot get path from playlist" }
            if (isSong)
                return File(
                    "$EXTERNAL_STORAGE_DIRECTORY/${
                        source.replaceFirst(
                            SONG_SOURCE_SHARED_STORAGE, ""
                        )
                    }"
                )
            return underlyingMediaId!!.path
        }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaId) return false

        if (source != other.source) return false
        if (underlyingMediaId != other.underlyingMediaId) return false

        return true
    }

    override fun toString(): String = Json.encodeToString(this)
}