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
     * * Is null if this is a song or a playlist without a specific media id attached
     * * Is a single media id pointing to a song if this is a loop or playlist with media id attached
     */
    var underlyingMediaId: MediaId? = null
) {

    companion object {
        /**
         * Points to e.g. 'storage/user/emulated/0/
         */
        const val SONG_SOURCE_SHARED_STORAGE = "*song-shared*"

        /**
         * Currently loops and playlists are stored in the settings file which means we don't have a path to a loop file
         */
        const val LOOP_SOURCE = "*loop*"
        const val PLAYLIST_SOURCE = "*playlist*"

        fun deserialize(json: String): MediaId = Json.decodeFromString(json)

        fun deserializeIfValid(json: String): MediaId? {
            return try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                null
            }
        }

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

        /**
         * TODO: Loops are currently not stored as files
         */
        fun fromPlaylist(playlistName: String, songOrLoopMediaId: MediaId? = null) =
            MediaId(PLAYLIST_SOURCE + playlistName, songOrLoopMediaId)

        val Empty: MediaId
            get() = MediaId("")
    }

    override fun toString(): String {
        return serialize()
    }

    val isSong: Boolean
        get() = source.contains(SONG_SOURCE_SHARED_STORAGE)

    val isLoop: Boolean
        get() = source.contains(LOOP_SOURCE)

    val isPlaylist: Boolean
        get() = source.contains(PLAYLIST_SOURCE)

    /**
     * Returns a new media id with only the [source] set and with [underlyingMediaId] = null
     */
    val baseMediaId: MediaId
        get() = MediaId(source)

    val path: File?
        get() {
            if (isSong) {
                return File(
                    Environment.getExternalStorageDirectory().absolutePath + "/" + source.replaceFirst(
                        SONG_SOURCE_SHARED_STORAGE, ""
                    )
                )
            }

            // Loops/playlists don't have path at the moment as they're stored in the settings file
            // But loops/playlists have an underlying playback
            return underlyingMediaId?.path
        }

    fun serialize(): String = Json.encodeToString(this)

    /**
     * Combines two media ids, the first one will be the [baseMediaId] and the second one the [underlyingMediaId]
     */
    operator fun plus(other: MediaId?) = MediaId(source, other)

    override fun equals(other: Any?): Boolean {
        return this === other && source == other.source && underlyingMediaId == other.underlyingMediaId
    }
}