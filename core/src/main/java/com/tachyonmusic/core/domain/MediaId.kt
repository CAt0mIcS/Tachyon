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

        fun ofLocalRemix(name: String, songMediaId: MediaId) =
            MediaId(PlaybackType.Remix.Local().toString() + name, songMediaId)

        fun ofLocalPlaylist(name: String) =
            MediaId(PlaybackType.Playlist.Local().toString() + name)

        val EMPTY = MediaId("")
    }

    val playbackType: PlaybackType
        get() =
            if (isLocalSong) PlaybackType.Song.Local()
            else if (isLocalRemix) PlaybackType.Remix.Local()
            else if (isLocalPlaylist) PlaybackType.Playlist.Local()
            else TODO("Invalid media id ${toString()}")

    val isLocalSong: Boolean
        get() = source.contains(PlaybackType.Song.Local().toString())

    val isLocalRemix: Boolean
        get() = source.contains(
            PlaybackType.Remix.Local().toString()
        ) && underlyingMediaId != null

    val isLocalPlaylist: Boolean
        get() = source.contains(PlaybackType.Playlist.Local().toString())

    val uri: Uri?
        get() {
            if (isLocalSong)
                return Uri.parse(
                    source.replaceFirst(PlaybackType.Song.Local().toString(), "")
                )
            return underlyingMediaId?.uri
        }

    val name: String?
        get() {
            if (isLocalRemix)
                return source.replace(PlaybackType.Remix.Local().toString(), "")
            if (isLocalPlaylist)
                return source.replace(PlaybackType.Playlist.Local().toString(), "")
            return null
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