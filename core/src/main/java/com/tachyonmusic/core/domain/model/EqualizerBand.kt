package com.tachyonmusic.core.domain.model

import android.os.Parcel
import android.os.Parcelable

data class EqualizerBand(
    val level: SoundLevel,
    val lowerBandFrequency: SoundFrequency,
    val upperBandFrequency: SoundFrequency,
    val centerFrequency: SoundFrequency
) : Parcelable {
    constructor(parcel: Parcel) : this(
        SoundLevel(parcel.readLong()),
        SoundFrequency(parcel.readLong()),
        SoundFrequency(parcel.readLong()),
        SoundFrequency(parcel.readLong())
    )

    override fun toString() =
        "${level.inmDb}|${upperBandFrequency.inmHz}|${lowerBandFrequency.inmHz}|${centerFrequency.inmHz}"

    companion object {
        fun fromString(str: String): EqualizerBand {
            val args = str.split('|')
            return EqualizerBand(
                args[0].toLong().mDb,
                args[2].toLong().mHz,
                args[1].toLong().mHz,
                args[3].toLong().mHz
            )
        }

        @JvmField
        val CREATOR = object : Parcelable.Creator<EqualizerBand> {
            override fun createFromParcel(parcel: Parcel) =EqualizerBand(parcel)
            override fun newArray(size: Int): Array<EqualizerBand?> = arrayOfNulls(size)
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(level.inmDb)
        parcel.writeLong(upperBandFrequency.inmHz)
        parcel.writeLong(lowerBandFrequency.inmHz)
        parcel.writeLong(centerFrequency.inmHz)
    }

    override fun describeContents() = 0
}