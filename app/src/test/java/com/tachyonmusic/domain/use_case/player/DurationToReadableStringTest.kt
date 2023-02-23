package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.toReadableString
import org.junit.Test


internal class DurationToReadableStringTest {

    @Test
    fun `Don't include milliseconds, returns correct result`() {
        assertEquals(10000.ms.toReadableString(false), "0:10")
        assertEquals(515385.ms.toReadableString(false), "8:35")
    }

    @Test
    fun `Include milliseconds, returns correct result`() {
        assertEquals(10000.ms.toReadableString(true), "0:10.000")
        assertEquals(526385.ms.toReadableString(true), "8:46.385")
    }
}