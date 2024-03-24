package com.tachyonmusic.core

import android.os.Parcel
import android.os.Parcelable

data class PlaybackParameters(
    val speed: Float,
    val pitch: Float,
    val volume: Float
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(speed)
        parcel.writeFloat(pitch)
        parcel.writeFloat(volume)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaybackParameters> {
        override fun createFromParcel(parcel: Parcel): PlaybackParameters {
            return PlaybackParameters(parcel)
        }

        override fun newArray(size: Int): Array<PlaybackParameters?> {
            return arrayOfNulls(size)
        }
    }
}