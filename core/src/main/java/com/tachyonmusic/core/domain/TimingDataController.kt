package com.tachyonmusic.core.domain

import android.os.Parcel
import android.os.Parcelable
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.Duration

class TimingDataController(
    private val _timingData: ArrayList<String> = arrayListOf(),
    currentIndex: Int = 0
) : Parcelable {
    var currentIndex: Int = 0
        private set

    val next: TimingData
        get() = nextTimingData()

    val current: TimingData
        get() = currentTimingData()

    val timingData: MutableList<TimingData>
        get() = _timingData.map { TimingData.deserialize(it) }.toMutableList()

    constructor(
        timingData: List<TimingData>,
        currentIndex: Int = 0
    ) : this(timingData.map { it.toString() } as ArrayList<String>, currentIndex)

    init {
        this.currentIndex = currentIndex
    }

    fun advanceToCurrentPosition(position: Duration) {
        currentIndex = getIndexOfCurrentPosition(position)
    }

    fun advanceToNext() {
        currentIndex++
        if (currentIndex >= _timingData.size)
            currentIndex = 0
    }

    fun getIndexOfCurrentPosition(position: Duration): Int {
        // TODO: Better way of ensuring that when a new playback is played, the first timing data is loaded first
        if (position == 0.ms)
            return 0

        for (i in _timingData.indices) {
            if (TimingData.deserialize(_timingData[i]).surrounds(position))
                return i
        }

        return closestTimingDataIndexAfter(position)
    }

    fun closestTimingDataIndexAfter(position: Duration): Int {
        var closestApproachIndex = 0
        var closestApproach = Int.MAX_VALUE
        for (i in _timingData.indices) {
            val distance =
                (TimingData.deserialize(_timingData[i]).startTime - position).inWholeMilliseconds.toInt()
            if (distance > 0 && distance < closestApproach) {
                closestApproach = distance
                closestApproachIndex = i
            }
        }
        return closestApproachIndex
    }

    fun anySurrounds(position: Duration): Boolean {
        for (item in _timingData)
            if (TimingData.deserialize(item).surrounds(position))
                return true
        return false
    }

    private fun nextTimingData(): TimingData {
        var nextIdx = currentIndex + 1
        if (nextIdx >= _timingData.size)
            nextIdx = 0
        return TimingData.deserialize(_timingData[nextIdx])
    }

    private fun currentTimingData() = TimingData.deserialize(_timingData[currentIndex])

    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeParcelableArray(this.toTypedArray(), flags)

        parcel.writeStringArray(_timingData.toTypedArray())
        parcel.writeInt(currentIndex)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TimingDataController> {
        override fun createFromParcel(parcel: Parcel): TimingDataController {
            // TODO: Better implementation for loading arrays/lists/...
//            val list = parcel.readParcelableArray(TimingData::class.java.classLoader)
//                ?.map { it as TimingData } ?: emptyList()

            val str = parcel.createStringArray()!!.toMutableList() as ArrayList<String>

            val index = parcel.readInt()
            return TimingDataController(str, index)
        }

        override fun newArray(size: Int): Array<TimingDataController?> = arrayOfNulls(size)
    }

    fun getOrNull(index: Int) = TimingData.deserializeIfValid(_timingData.getOrNull(index))
    fun isEmpty() = _timingData.isEmpty()
    fun isNotEmpty() = _timingData.isNotEmpty()
    val size get() = _timingData.size
    val indices get() = _timingData.indices
    operator fun get(index: Int) = TimingData.deserialize(_timingData[index])

    override fun equals(other: Any?): Boolean {
        if (other !is TimingDataController) return false
        return other._timingData == _timingData && other.currentIndex == currentIndex
    }
}

