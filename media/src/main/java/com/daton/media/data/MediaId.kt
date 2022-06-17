package com.daton.media.data

import android.os.Environment
import com.daton.media.device.MediaSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class MediaId(
    /**
     * Specifies the relative location of this media, e.g.
     * [SOURCE_INTERNAL_STORAGE] + "/data/Test1.loop"
     * [SOURCE_SHARED_STORAGE] + "/Music/SomeSong.mp3"
     */
    var source: String,

    /**
     * Specifies a possible media id in this media id
     * * Is null if this is a song
     * * Is a single media id pointing to a song if this is a loop or playlist
     */
    var underlyingMediaId: MediaId? = null
) {

    companion object {
        /**
         * Points to e.g. data/user/0/com.daton.mucify/
         */
        const val SONG_SOURCE_INTERNAL_STORAGE = "*song-internal*"

        /**
         * Points to e.g. 'storage/user/emulated/
         */
        const val SONG_SOURCE_SHARED_STORAGE = "*song-shared*"

        /**
         * Currently loops and playlists are stored in the settings file which means we don't have a path to a loop file
         */
        const val LOOP_SOURCE = "*loop*"
        const val PLAYLIST_SOURCE = "*playlist*"

        fun deserialize(json: String): MediaId = Json.decodeFromString(json)

        /**
         * TODO: Song files must currently be in shared storage
         */
        fun fromSongFile(file: File): MediaId {
            return MediaId(
                SONG_SOURCE_SHARED_STORAGE + file.absolutePath.substring(
                    file.absolutePath.indexOf(
                        Environment.getExternalStorageDirectory().absolutePath
                    ) + Environment.getExternalStorageDirectory().absolutePath.length
                )
            )
        }

        /**
         * TODO: Loops are currently not stored as files
         */
        fun fromLoop(loopName: String, songMediaId: MediaId) =
            MediaId(LOOP_SOURCE + loopName, songMediaId)

        val Empty = MediaId("")
    }

    override fun toString(): String {
        return serialize()
    }

    val isSong: Boolean
        get() = MediaSource.SupportedAudioExtensions.contains(File(source).extension)

    val isLoop: Boolean
        get() = source.contains(LOOP_SOURCE)

    val isPlaylist: Boolean
        get() = source.contains(PLAYLIST_SOURCE)

    val isStoredInternally: Boolean
        get() = source.substring(
            0,
            SONG_SOURCE_INTERNAL_STORAGE.length
        ) == SONG_SOURCE_INTERNAL_STORAGE

    val isStoredShared: Boolean
        get() = source.substring(0, SONG_SOURCE_SHARED_STORAGE.length) == SONG_SOURCE_SHARED_STORAGE

    val path: File?
        get() {
            if (isStoredInternally)
                return File("./" + source.replaceFirst(SONG_SOURCE_INTERNAL_STORAGE, ""))
            else if (isStoredShared)
                return File(
                    Environment.getExternalStorageDirectory().absolutePath + "/" + source.replaceFirst(
                        SONG_SOURCE_SHARED_STORAGE, ""
                    )
                )
            // Loops/playlists don't have path at the moment as they're stored in the settings file
            // But loops/playlists have an underlying playback
            return underlyingMediaId?.path
        }

    fun serialize(): String = Json.encodeToString(this)

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MediaId)
            return false
        return serialize() == other.serialize()
    }
}