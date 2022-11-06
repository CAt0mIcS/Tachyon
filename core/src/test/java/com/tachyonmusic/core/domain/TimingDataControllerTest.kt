package com.tachyonmusic.core.domain

import com.tachyonmusic.testutils.assertEquals
import org.junit.Test


class TimingDataControllerTest {
    @Test
    fun `Advancing timing data advances to correct timing data`() {
        val timingData = loadInOrderTimingData()

        for (i in timingData.indices) {
            assertEquals(timingData.currentIndex, i)
            assertEquals(timingData.current, timingData[i])

            timingData.advanceToNext()
        }

        // Check that we advance to the first one after the last one
        assertEquals(timingData.currentIndex, 0)
        assertEquals(timingData.current, timingData[0])
    }

    @Test
    fun `getIndexOfCurrentPosition gets the correct index in ordered list`() {
        val timingData = loadInOrderTimingData()
        assertEquals(timingData.getIndexOfCurrentPosition(0), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(36), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(63), 3)
        assertEquals(timingData.getIndexOfCurrentPosition(90), 0)
    }

    @Test
    fun `getIndexOfCurrentPosition gets the correct index in intersecting list`() {
        val timingData = loadIntersectingTimingData()
        assertEquals(timingData.getIndexOfCurrentPosition(0), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(19), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(24), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(31), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(40), 4)
        assertEquals(timingData.getIndexOfCurrentPosition(70), 2)
    }

    @Test
    fun `getIndexOfCurrentPosition gets the first index when position is 0 and there's no timing data surrounding 0`() {
        val timingData = TimingDataController(
            listOf(
                TimingData(48, 90).toString(),
                TimingData(10, 20).toString(),
            )
        )

        assertEquals(timingData.getIndexOfCurrentPosition(0), 0)
    }

    /**
     * An ordered timing data list refers to a list of timing data where start and end times don't
     * intersect and the next start time is always after the last end time
     */
    private fun loadInOrderTimingData() = TimingDataController(
        listOf(
            TimingData(10, 20).toString(),
            TimingData(24, 40).toString(),
            TimingData(46, 59).toString(),
            TimingData(69, 80).toString(),
        )
    )

    /**
     * An intersecting timing data list refers to an unordered list of timing data where we
     * can have a start time which is less than the end time of another timing data item
     */
    private fun loadIntersectingTimingData() = TimingDataController(
        listOf(
            TimingData(28, 32).toString(),
            TimingData(18, 26).toString(),
            TimingData(70, 5).toString(),
            TimingData(10, 20).toString(),
            TimingData(30, 60).toString(),
        )
    )
}