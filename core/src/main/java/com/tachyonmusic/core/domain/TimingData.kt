package com.tachyonmusic.core.domain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

/**
 * Class which holds one start and end time of a loop
 * TODO: How much performance do we get out of providing our own serializer
 * TODO: Maybe make parcelable, depending on performance of above
 */
@Serializable(with = TimingData.Serializer::class)
data class TimingData(
    var startTime: Long,
    var endTime: Long
) {
    fun surrounds(playerPosition: Long) = playerPosition in startTime..endTime

    class Serializer : KSerializer<TimingData> {
        override fun deserialize(decoder: Decoder): TimingData {
            val strings = decoder.decodeString().split('|')
            return TimingData(strings[0].toLong(), strings[1].toLong())
        }

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("timingData", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: TimingData) {
            encoder.encodeString("${value.startTime}|${value.endTime}")
        }
    }

    override fun toString(): String = Json.encodeToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimingData) return false

        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        return result
    }


    companion object {
        fun deserialize(value: String): TimingData = Json.decodeFromString(value)
        fun deserializeIfValid(value: String?): TimingData? {
            return try {
                Json.decodeFromString(value ?: "")
            } catch (e: Exception) {
                null
            }
        }

        fun fromStringArray(array: Array<String>) = array.map { deserialize(it) } as ArrayList

        fun toStringArray(array: List<TimingData>): Array<String> =
            array.map { it.toString() }.toTypedArray()
    }
}