package com.tachyonmusic.core.domain

import android.os.Environment
import com.tachyonmusic.core.constants.Constants
import com.tachyonmusic.core.constants.PlaybackType
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File

@Serializable(with = MediaId.Serializer::class)
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
                Json.decodeFromString(value)
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
                    "$Constants.EXTERNAL_STORAGE_DIRECTORY/${
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

    class Serializer : KSerializer<MediaId> {
        override fun deserialize(decoder: Decoder): MediaId =
            deserialize(decoder.decodeString())

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("mediaId", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: MediaId) =
            encoder.encodeString(toString())
    }
}