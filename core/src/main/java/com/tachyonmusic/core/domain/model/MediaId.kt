package com.tachyonmusic.core.domain.model

import android.os.Environment
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

        // Stored for performance reasons
        val EXTERNAL_STORAGE_DIRECTORY: String =
            Environment.getExternalStorageDirectory().absolutePath
    }

    constructor(song: Song) : this(
        Playback.Type.SongSharedStorage.toString() +
                song.path.absolutePath.substring(
                    song.path.absolutePath.indexOf(
                        EXTERNAL_STORAGE_DIRECTORY
                    ) + EXTERNAL_STORAGE_DIRECTORY.length
                )
    )

    constructor(loop: Loop) : this(Playback.Type.Loop.toString() + loop.name, loop.song.mediaId)

    constructor(playlist: Playlist) : this(Playback.Type.Playlist.toString() + playlist.name)

    val isSong: Boolean
        get() = source.contains(Playback.Type.SongSharedStorage.toString())

    val isLoop: Boolean
        get() = source.contains(Playback.Type.Loop.toString()) && underlyingMediaId != null

    val isPlaylist: Boolean
        get() = source.contains(Playback.Type.Playlist.toString())

    val path: File
        get() {
            assert(!isPlaylist || underlyingMediaId != null) { "Cannot get path from playlist" }
            if (isSong)
                return File(
                    "$EXTERNAL_STORAGE_DIRECTORY/${
                        source.replaceFirst(
                            Playback.Type.SongSharedStorage.toString(), ""
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

    override fun toString(): String {
        return if (underlyingMediaId != null)
            "${source}|||${underlyingMediaId}"
        else
            source
    }

    class Serializer : KSerializer<MediaId> {
        override fun deserialize(decoder: Decoder): MediaId =
            Companion.deserialize(decoder.decodeString())

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("mediaId", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: MediaId) = encoder.encodeString(toString())
    }
}