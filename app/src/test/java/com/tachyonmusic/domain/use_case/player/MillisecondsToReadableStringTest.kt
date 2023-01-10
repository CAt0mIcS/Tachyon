package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.testutils.assertEquals
import org.junit.Test


internal class MillisecondsToReadableStringTest {

    val convert = MillisecondsToReadableString()

    @Test
    fun `Don't include milliseconds, returns correct result`() {
        assertEquals(convert(10000L, false), "0:10")
        assertEquals(convert(515385L, false), "8:35")
    }

    @Test
    fun `Include milliseconds, returns correct result`() {
        assertEquals(convert(10000L, true), "0:10.000")
        assertEquals(convert(526385L, true), "8:46.385")
    }

    @Test
    fun `Invalid milliseconds, returns empty string`() {
        assertEquals(convert(null, true), "")
        assertEquals(convert(null, false), "")
    }

}