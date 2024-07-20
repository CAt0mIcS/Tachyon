package com.tachyonmusic.database.domain.model

import androidx.room.Embedded
import androidx.room.Entity
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.ReverbConfig
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.model.EqualizerBand
import com.tachyonmusic.util.Duration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

const val REMIX_DATABASE_TABLE_NAME = "CustomizedSongs"

@Serializable(with = RemixEntitySerializer::class)
@Entity(tableName = REMIX_DATABASE_TABLE_NAME)
class RemixEntity(
    mediaId: MediaId,
    val songTitle: String,
    val songArtist: String,
    val songDuration: Duration,
    val timingData: List<TimingData>? = null,
    val currentTimingDataIndex: Int = 0,

    val bassBoost: Int? = null,
    val virtualizerStrength: Int? = null,
    val equalizerBands: List<EqualizerBand>? = null,

    @Embedded
    val playbackParameters: PlaybackParameters? = null,

    @Embedded
    val reverb: ReverbConfig? = null
) : SinglePlaybackEntity(mediaId, songTitle, songArtist, songDuration)


object RemixEntitySerializer : KSerializer<RemixEntity> {
    override val descriptor = buildClassSerialDescriptor("CustomizedSongEntity") {
        element<String>("MediaId")
        element<String>("SongTitle")
        element<String>("SongArtist")
        element<Long>("SongDuration")
        element<List<String>>("TimingData", isOptional = true)
        element<Int>("CurrentTimingDataIndex", isOptional = true)
        element<Int?>("BassBoost", isOptional = true)
        element<Int?>("VirtualizerStrength", isOptional = true)
        element<List<String>?>("EqualizerBands", isOptional = true)
        element<PlaybackParameters?>("PlaybackParameters", isOptional = true)
        element<ReverbConfig?>("ReverbConfig", isOptional = true)
    }

    override fun deserialize(decoder: Decoder) = decoder.decodeStructure(descriptor) {
        var mediaId: MediaId? = null
        var songTitle: String? = null
        var songArtist: String? = null
        var songDuration: Duration? = null
        var timingData: List<TimingData>? = null
        var currentTimingDataIndex = 0
        var bassBoost: Int? = null
        var virtualizerStrength: Int? = null
        var equalizerBands: List<EqualizerBand>? = null
        var playbackParameters: PlaybackParameters? = null
        var reverb: ReverbConfig? = null

        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop

                0 -> mediaId = MediaId.deserialize(decodeStringElement(descriptor, 0))
                1 -> songTitle = decodeStringElement(descriptor, 1)
                2 -> songArtist = decodeStringElement(descriptor, 2)
                3 -> songDuration = Duration(decodeLongElement(descriptor, 3))
                4 -> timingData = decodeNullableSerializableElement(
                    descriptor,
                    4,
                    ListSerializer(String.serializer())
                )?.map { TimingData.deserialize(it) }

                5 -> currentTimingDataIndex = decodeIntElement(descriptor, 5)
                6 -> {
                    val i = decodeIntElement(descriptor, 6)
                    bassBoost = if (i == Int.MIN_VALUE) null else i
                }

                7 -> {
                    val i = decodeIntElement(descriptor, 7)
                    virtualizerStrength = if (i == Int.MIN_VALUE) null else i
                }

                8 -> equalizerBands = decodeNullableSerializableElement(
                    descriptor,
                    8,
                    ListSerializer(String.serializer())
                )?.map { EqualizerBand.fromString(it) }

                9 -> playbackParameters = decodeNullableSerializableElement(
                    descriptor,
                    9,
                    PlaybackParameters.serializer()
                )

                10 -> reverb =
                    decodeNullableSerializableElement(descriptor, 10, ReverbConfig.serializer())

                else -> throw SerializationException("Unexpected index $index")
            }
        }

        RemixEntity(
            mediaId!!,
            songTitle!!,
            songArtist!!,
            songDuration!!,
            timingData,
            currentTimingDataIndex,
            bassBoost,
            virtualizerStrength,
            equalizerBands,
            playbackParameters,
            reverb
        )
    }

    override fun serialize(encoder: Encoder, value: RemixEntity) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.mediaId.toString())
            encodeStringElement(descriptor, 1, value.songTitle)
            encodeStringElement(descriptor, 2, value.songArtist)
            encodeLongElement(descriptor, 3, value.songDuration.inWholeMilliseconds)
            encodeNullableSerializableElement(
                descriptor,
                4,
                ListSerializer(String.serializer()),
                value.timingData?.map { it.toString() })
            encodeIntElement(descriptor, 5, value.currentTimingDataIndex)
            encodeIntElement(descriptor, 6, value.bassBoost ?: Int.MIN_VALUE)
            encodeIntElement(descriptor, 7, value.virtualizerStrength ?: Int.MIN_VALUE)
            encodeNullableSerializableElement(
                descriptor,
                8,
                ListSerializer(String.serializer()),
                value.equalizerBands?.map { it.toString() })
            encodeNullableSerializableElement(
                descriptor,
                9,
                PlaybackParameters.serializer(),
                value.playbackParameters
            )
            encodeNullableSerializableElement(
                descriptor,
                10,
                ReverbConfig.serializer(),
                value.reverb
            )
        }
    }
}