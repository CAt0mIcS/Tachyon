package com.tachyonmusic.core.domain

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Class which holds one start and end time of a loop
 * TODO: How much performance do we get out of providing our own serializer
 * TODO: Maybe make parcelable, depending on performance of above
 */
data class TimingData(
    var startTime: Long,
    var endTime: Long
) {
    fun surrounds(playerPosition: Long) =
        if (startTime < endTime) playerPosition in startTime..endTime
        else playerPosition >= startTime || playerPosition <= endTime // TODO: <= or just < ||| >= or just >

    override fun toString(): String = "${startTime}|${endTime}"

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
        fun deserialize(value: String): TimingData {
            val strings = value.split('|')
            return TimingData(strings[0].toLong(), strings[1].toLong())
        }

        fun deserializeIfValid(value: String?): TimingData? {
            return try {
                deserialize(value ?: "")
            } catch (e: Exception) {
                null
            }
        }
    }

    class Serializer : TypeAdapter<TimingData>() {
        override fun read(reader: JsonReader): TimingData? {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            return deserializeIfValid(reader.nextString())
        }

        override fun write(writer: JsonWriter, value: TimingData?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(value.toString())
        }
    }
}