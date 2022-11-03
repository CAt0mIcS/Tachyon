package com.tachyonmusic.core.domain

import android.os.Parcel
import android.os.Parcelable

class TimingDataController(
    timingData: List<TimingData> = emptyList(),
    currentIndex: Int = 0
) : ArrayList<TimingData>(), Parcelable {
    var currentIndex: Int = 0
        private set

    val next: TimingData
        get() = nextTimingData()

    val current: TimingData
        get() = currentTimingData()

    init {
        this.currentIndex = currentIndex
        addAll(timingData)
    }

    fun advanceToCurrentPosition(positionMs: Long) {
        currentIndex = getIndexOfCurrentPosition(positionMs)
    }

    fun advanceToNext() {
        currentIndex++
        if (currentIndex >= size)
            currentIndex = 0
    }

    fun getIndexOfCurrentPosition(positionMs: Long): Int {
        for (i in indices) {
            if (this[i].surrounds(positionMs))
                return i
        }

        return closestTimingDataIndexAfter(positionMs)
    }

    fun closestTimingDataIndexAfter(positionMs: Long): Int {
        var closestApproachIndex = 0
        var closestApproach = Int.MAX_VALUE
        for (i in indices) {
            val distance = (this[i].startTime - positionMs).toInt()
            if (distance > 0 && distance < closestApproach) {
                closestApproach = distance
                closestApproachIndex = i
            }
        }
        return closestApproachIndex
    }

    fun anySurrounds(positionMs: Long): Boolean {
        for (item in this)
            if (item.surrounds(positionMs))
                return true
        return false
    }

    private fun nextTimingData(): TimingData {
        var nextIdx = currentIndex + 1
        if (nextIdx >= size)
            nextIdx = 0
        return this[nextIdx]
    }

    private fun currentTimingData() = this[currentIndex]

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelableArray(this.toTypedArray(), flags)
        parcel.writeInt(currentIndex)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TimingDataController> {
        override fun createFromParcel(parcel: Parcel): TimingDataController {
            // TODO: Better implementation for loading arrays/lists/...

            return TimingDataController(
                parcel.readParcelableArray(TimingData::class.java.classLoader)
                    ?.map { it as TimingData } ?: emptyList(), parcel.readInt()
            )
        }

        override fun newArray(size: Int): Array<TimingDataController?> = arrayOfNulls(size)
    }
}

