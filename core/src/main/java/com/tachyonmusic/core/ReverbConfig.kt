package com.tachyonmusic.core

import android.os.Parcel
import android.os.Parcelable

// TODO: Define defaults
data class ReverbConfig(
    val roomLevel: Int,
    val roomHFLevel: Int,
    val decayTime: Int,
    val decayHFRatio: Int,
    val reflectionsLevel: Int,
    val reflectionsDelay: Int,
    val reverbLevel: Int,
    val reverbDelay: Int,
    val diffusion: Int,
    val density: Int
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