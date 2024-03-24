package com.tachyonmusic.core.domain

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import java.security.InvalidParameterException

/**
 * Class which holds one start and end time of a customizedSong
 * TODO: How much performance do we get out of providing our own serializer
 */
data class TimingData(
    var startTime: Duration,
    var endTime: Duration
) : Parcelable {

    init {
        if (startTime < 0.ms || endTime < 0.ms)
            throw InvalidParameterException("Start-/EndTime must be above 0: start: $startTime, end: $endTime")
    }

    constructor(parcel: Parcel) : this(
        parcel.readLong().ms,
        parcel.readLong().ms
    )

    fun surrounds(playerPosition: Duration) =
        if (startTime < endTime) playerPosition in startTime..endTime
        else playerPosition >= startTime || playerPosition <= endTime // TODO: <= or just < ||| >= or just >

    override fun toString(): String =
        "${startTime.inWholeMilliseconds}|${endTime.inWholeMilliseconds}"

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

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(startTime.inWholeMilliseconds)
        parcel.writeLong(endTime.inWholeMilliseconds)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TimingData> {
            override fun createFromParcel(parcel: Parcel) = TimingData(parcel)
            override fun newArray(size: Int): Array<TimingData?> = arrayOfNulls(size)
        }

        fun deserialize(value: String): TimingData {
            val strings = value.split('|')
            return TimingData(strings[0].toLong().ms, strings[1].toLong().ms)
        }

        fun deserializeIfValid(value: String?): TimingData? {
            return try {
                deserialize(value ?: "")
            } catch (e: Exception) {
                null
            }
        }
    }
}