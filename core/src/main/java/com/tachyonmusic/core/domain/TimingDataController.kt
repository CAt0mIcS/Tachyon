package com.tachyonmusic.core.domain

class TimingDataController(
    timingData: List<TimingData> = emptyList()
) : ArrayList<TimingData>() {
    private var currentTimingDataIndex: Int = 0

    val next: TimingData
        get() = nextTimingData()

    val current: TimingData
        get() = currentTimingData()

    init {
        addAll(timingData)
    }

    fun advanceToCurrentPosition(positionMs: Long) {
        currentTimingDataIndex = getIndexOfCurrentPosition(positionMs)
    }

    fun advanceToNext() {
        currentTimingDataIndex++
        if (currentTimingDataIndex >= size)
            currentTimingDataIndex = 0
    }

    fun getIndexOfCurrentPosition(positionMs: Long): Int {
        if (positionMs == 0L)
            return 0

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

    private fun nextTimingData(): TimingData {
        var nextIdx = currentTimingDataIndex + 1
        if (nextIdx >= size)
            nextIdx = 0
        return this[nextIdx]
    }

    private fun currentTimingData() = this[currentTimingDataIndex]

    fun toStringArray(): Array<String> = map { it.toString() }.toTypedArray()

    companion object {
        fun fromStringArray(array: Array<String>) =
            TimingDataController(array.map { TimingData.deserialize(it) })
    }
}

