package com.tachyonmusic.core.domain

import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.util.ms
import org.junit.Test


internal class TimingDataControllerTest {
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
        assertEquals(timingData.getIndexOfCurrentPosition(0.ms), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(36.ms), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(63.ms), 3)
        assertEquals(timingData.getIndexOfCurrentPosition(90.ms), 0)
    }

    @Test
    fun `getIndexOfCurrentPosition gets the correct index in intersecting list`() {
        val timingData = loadIntersectingTimingData()
        assertEquals(timingData.getIndexOfCurrentPosition(0.ms), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(19.ms), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(24.ms), 1)
        assertEquals(timingData.getIndexOfCurrentPosition(31.ms), 0)
        assertEquals(timingData.getIndexOfCurrentPosition(40.ms), 4)
        assertEquals(timingData.getIndexOfCurrentPosition(70.ms), 2)
    }

    @Test
    fun `getIndexOfCurrentPosition gets the first index when position is 0 and there's no timing data surrounding 0`() {
        val timingData = TimingDataController(
            listOf(
                TimingData(48.ms, 90.ms),
                TimingData(10.ms, 20.ms),
            )
        )

        assertEquals(timingData.getIndexOfCurrentPosition(0.ms), 0)
    }

    /**
     * An ordered timing data list refers to a list of timing data where start and end times don't
     * intersect and the next start time is always after the last end time
     */
    private fun loadInOrderTimingData() = TimingDataController(
        listOf(
            TimingData(10.ms, 20.ms),
            TimingData(24.ms, 40.ms),
            TimingData(46.ms, 59.ms),
            TimingData(69.ms, 80.ms),
        )
    )

    /**
     * An intersecting timing data list refers to an unordered list of timing data where we
     * can have a start time which is less than the end time of another timing data item
     */
    private fun loadIntersectingTimingData() = TimingDataController(
        listOf(
            TimingData(28.ms, 32.ms),
            TimingData(18.ms, 26.ms),
            TimingData(70.ms, 5.ms),
            TimingData(10.ms, 20.ms),
            TimingData(30.ms, 60.ms),
        )
    )

    // TODO: Test if Duration.sec, Duration.min, ... also work
}