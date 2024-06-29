package com.tachyonmusic.database.util

import com.tachyonmusic.core.domain.MediaId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class MediaIdSerializer : KSerializer<MediaId> {
    override val descriptor = PrimitiveSerialDescriptor("MediaId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) = MediaId.deserialize(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: MediaId) {
        encoder.encodeString(value.toString())
    }
}