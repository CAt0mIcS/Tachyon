package com.tachyonmusic.core

import android.os.Parcel
import android.os.Parcelable

// TODO: Define proper defaults
data class ReverbConfig(
    val roomLevel: Int = 0,
    val roomHFLevel: Int = 0,
    val decayTime: Int = 100,
    val decayHFRatio: Int = 1000,
    val reflectionsLevel: Int = 0,
    val reflectionsDelay: Int = 0,
    val reverbLevel: Int = 0,
    val reverbDelay: Int = 0,
    val diffusion: Int = 0,
    val density: Int = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(roomLevel)
        parcel.writeInt(roomHFLevel)
        parcel.writeInt(decayTime)
        parcel.writeInt(decayHFRatio)
        parcel.writeInt(reflectionsLevel)
        parcel.writeInt(reflectionsDelay)
        parcel.writeInt(reverbLevel)
        parcel.writeInt(reverbDelay)
        parcel.writeInt(diffusion)
        parcel.writeInt(density)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReverbConfig> {
        override fun createFromParcel(parcel: Parcel): ReverbConfig {
            return ReverbConfig(parcel)
        }

        override fun newArray(size: Int): Array<ReverbConfig?> {
            return arrayOfNulls(size)
        }
    }
}