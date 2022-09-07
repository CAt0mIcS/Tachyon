package com.tachyonmusic.core.domain

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.tachyonmusic.core.constants.Constants
import com.tachyonmusic.core.constants.PlaybackType
import java.io.File

class MediaId(val source: String, val underlyingMediaId: MediaId? = null) {

    companion object {
        fun deserialize(value: String): MediaId {
            val strings = value.split("|||", ignoreCase = false, limit = 2)
            return if (strings.size == 1)
                MediaId(strings[0])
            else
                MediaId(strings[0], deserialize(strings[1]))
        }

        fun deserializeIfValid(value: String): MediaId? =
            try {
                deserialize(value)
            } catch (e: Exception) {
                null
            }

        fun ofLocalSong(path: File) =
            MediaId(
                PlaybackType.Song.Local().toString() + path.absolutePath.substring(
                    path.absolutePath.indexOf(
                        Constants.EXTERNAL_STORAGE_DIRECTORY
                    ) + Constants.EXTERNAL_STORAGE_DIRECTORY.length
                )
            )

        fun ofRemoteLoop(name: String, songMediaId: MediaId) =
            MediaId(PlaybackType.Loop.Remote().toString() + name, songMediaId)

        fun ofRemotePlaylist(name: String) =
            MediaId(PlaybackType.Playlist.Remote().toString() + name)
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

    val path: File?
        get() {
            if (isLocalSong)
                return File(
                    "${Constants.EXTERNAL_STORAGE_DIRECTORY}/${
                        source.replaceFirst(
                            PlaybackType.Song.Local().toString(), ""
                        )
                    }"
                )
            return underlyingMediaId?.path
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaId) return false

        if (source != other.source) return false
        if (underlyingMediaId != other.underlyingMediaId) return false

        return true
    }

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