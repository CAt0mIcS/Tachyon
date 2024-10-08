package com.tachyonmusic.core

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class PlaybackParameters(
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val volume: Float = 1f
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

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<PlaybackParameters> {
        override fun createFromParcel(parcel: Parcel)=PlaybackParameters(parcel)
        override fun newArray(size: Int): Array<PlaybackParameters?> =arrayOfNulls(size)
    }
}