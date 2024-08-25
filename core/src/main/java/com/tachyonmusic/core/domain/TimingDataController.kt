package com.tachyonmusic.core.domain

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.ms
import kotlin.contracts.contract

data class TimingDataController(
    val timingData: List<TimingData> = listOf(),
    var currentIndex: Int = 0
) : Parcelable {
    val next: TimingData
        get() = nextTimingData()

    val current: TimingData
        get() = currentTimingData()


    fun coversDuration(start: Duration, end: Duration) =
        timingData.find { it.startTime == start && it.endTime == end } != null

    fun advanceToIndex(i: Int) {
        if (i < timingData.size && i >= 0)
            currentIndex = i
    }

    fun advanceToCurrentPosition(position: Duration) {
        currentIndex = getIndexOfCurrentPosition(position)
    }

    fun advanceToNext() {
        currentIndex++
        if (currentIndex >= timingData.size)
            currentIndex = 0
    }

    fun getIndexOfCurrentPosition(position: Duration): Int {
        // TODO: Better way of ensuring that when a new playback is played, the first timing data is loaded first
        if (position == 0.ms)
            return 0

        for (i in timingData.indices) {
            if (timingData[i].surrounds(position))
                return i
        }

        return closestTimingDataIndexAfter(position)
    }

    fun closestTimingDataIndexAfter(position: Duration): Int {
        var closestApproachIndex = 0
        var closestApproach = Int.MAX_VALUE
        for (i in timingData.indices) {
            val distance =
                (timingData[i].startTime - position).inWholeMilliseconds.toInt()
            if (distance in 1 until closestApproach) {
                closestApproach = distance
                closestApproachIndex = i
            }
        }
        return closestApproachIndex
    }

    fun anySurrounds(position: Duration): Boolean {
        for (item in timingData)
            if (item.surrounds(position))
                return true
        return false
    }

    private fun nextTimingData(): TimingData {
        var nextIdx = currentIndex + 1
        if (nextIdx >= timingData.size)
            nextIdx = 0
        return timingData[nextIdx]
    }

    private fun currentTimingData() = timingData[currentIndex]

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedArray(timingData.toTypedArray(), flags)
        parcel.writeInt(currentIndex)
    }

    override fun describeContents() = 0

    override fun toString() =
        currentIndex.toString() + "--" + timingData.joinToString(separator = ";") { it.toString() }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TimingDataController> {
            override fun createFromParcel(parcel: Parcel): TimingDataController {
                val timingData = parcel.createTypedArray(TimingData.CREATOR)!!

                val index = parcel.readInt()
                return TimingDataController(timingData.toList(), index)
            }

            override fun newArray(size: Int): Array<TimingDataController?> = arrayOfNulls(size)
        }

        fun default(end: Duration) = TimingDataController(listOf(TimingData(0.ms, end)))
    }

    fun getOrNull(index: Int) = timingData.getOrNull(index)
    fun isEmpty() = timingData.isEmpty()
    fun isNotEmpty() = timingData.isNotEmpty()
    val size get() = timingData.size
    val indices get() = timingData.indices
    fun first() = timingData.first()
    fun last() = timingData.last()
    operator fun get(index: Int) = timingData[index]

    override fun equals(other: Any?): Boolean {
        if (other !is TimingDataController) return false
        return timingData == other.timingData && currentIndex == other.currentIndex
    }
}

fun TimingDataController?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }

    return this == null || timingData.isEmpty()
}