package com.tachyonmusic.database.domain.model

import androidx.room.Entity
import com.tachyonmusic.core.domain.MediaId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

const val PLAYLIST_DATABASE_TABLE_NAME = "Playlists"

@Serializable(with = PlaylistEntitySerializer::class)
@Entity(tableName = PLAYLIST_DATABASE_TABLE_NAME)
class PlaylistEntity(
    val name: String,
    mediaId: MediaId,
    val items: List<MediaId>,
    val currentItemIndex: Int = 0,

    timestampCreatedAddedEdited: Long = System.currentTimeMillis(),
) : PlaybackEntity(mediaId, timestampCreatedAddedEdited)


object PlaylistEntitySerializer : KSerializer<PlaylistEntity> {
    override val descriptor = buildClassSerialDescriptor("PlaylistEntity") {
        element<String>("Name")
        element<String>("MediaId")
        element<List<String>>("Items")
        element<Int>("CurrentItemIndex", isOptional = true)
        element<Long>("TimestampCreatedAddedEdited", isOptional = true)
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var name: String? = null
        var mediaId: MediaId? = null
        var items: List<MediaId>? = null
        var currentItemIndex = 0
        var timestampCreatedAddedEdited = 0L

        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop

                0 -> name = decodeStringElement(descriptor, 0)
                1 -> mediaId = MediaId.deserializeIfValid(decodeStringElement(descriptor, 1))
                2 -> items = decodeSerializableElement(
                    descriptor,
                    2,
                    ListSerializer(String.serializer())
                ).map { MediaId.deserialize(it) }

                3 -> currentItemIndex = decodeIntElement(descriptor, 3)
                4 -> timestampCreatedAddedEdited = decodeLongElement(descriptor, 4)

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        PlaylistEntity(name!!, mediaId!!, items!!, currentItemIndex, timestampCreatedAddedEdited)
    }

    override fun serialize(encoder: Encoder, value: PlaylistEntity) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.mediaId.toString())
            encodeSerializableElement(
                descriptor,
                2,
                ListSerializer(String.serializer()),
                value.items.map { it.toString() })
            encodeIntElement(descriptor, 3, value.currentItemIndex)
            encodeLongElement(descriptor, 4, value.timestampCreatedAddedEdited)
        }
    }
}
