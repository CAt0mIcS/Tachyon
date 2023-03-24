package com.tachyonmusic.core.domain

import android.net.Uri
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.tachyonmusic.core.data.constants.PlaybackType

data class MediaId(
    val source: String,
    val underlyingMediaId: MediaId? = null
) {

    companion object {
        fun deserialize(value: String): MediaId {
            val strings = value.split("|||", ignoreCase = false, limit = 2)
            return if (strings.size == 1)
                MediaId(strings[0])
            else
                MediaId(strings[0], deserialize(strings[1]))
        }

        fun deserializeIfValid(value: String?): MediaId? =
            try {
                deserialize(value ?: "")
            } catch (e: Exception) {
                null
            }

        fun ofLocalSong(uri: Uri) =
            MediaId(PlaybackType.Song.Local().toString() + uri.toString())

        fun ofRemoteLoop(name: String, songMediaId: MediaId) =
            MediaId(PlaybackType.Loop.Remote().toString() + name, songMediaId)

        fun ofRemotePlaylist(name: String) =
            MediaId(PlaybackType.Playlist.Remote().toString() + name)

        val EMPTY = MediaId("")
    }

    val playbackType: PlaybackType
        get() =
            if (isLocalSong) PlaybackType.Song.Local()
            else if (isRemoteLoop) PlaybackType.Loop.Remote()
            else if (isRemotePlaylist) PlaybackType.Playlist.Remote()
            else TODO("Invalid media id ${toString()}")

    val isLocalSong: Boolean
        get() = source.contains(PlaybackType.Song.Local().toString())

    val isRemoteLoop: Boolean
        get() = source.contains(PlaybackType.Loop.Remote().toString()) && underlyingMediaId != null

    val isRemotePlaylist: Boolean
        get() = source.contains(PlaybackType.Playlist.Remote().toString())

    val uri: Uri?
        get() {
            if (isLocalSong)
                return Uri.parse(
                    source.replaceFirst(PlaybackType.Song.Local().toString(), "")
                )
            return underlyingMediaId?.uri
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaId) return false

        if (source != other.source) return false
        if (underlyingMediaId != other.underlyingMediaId) return false

        return true
    }

    override fun hashCode(): Int = source.hashCode() + (underlyingMediaId?.hashCode() ?: 0)

    override fun toString(): String {
        return if (underlyingMediaId != null)
            "${source}|||${underlyingMediaId}"
        else
            source
    }

    class Serializer : TypeAdapter<MediaId>() {
        override fun read(reader: JsonReader): MediaId? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            return deserializeIfValid(reader.nextString())
        }

        override fun write(writer: JsonWriter, value: MediaId?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value.toString())
        }
    }
}