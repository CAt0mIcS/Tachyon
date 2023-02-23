package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.ms
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class NormalizeCurrentPositionTest {

    private val browser: MediaBrowserController = mockk()
    private val normalizeCurrentPosition = NormalizeCurrentPosition(browser)

    @Test
    fun `Position returns correct normalized position`() {
        every { browser.currentPosition } returns 10000.ms
        every { browser.duration } returns 12568204.ms
        val expectedNormalized =
            browser.currentPosition!!.inWholeMilliseconds.toFloat() / browser.duration!!.inWholeMilliseconds.toFloat()

        assert(normalizeCurrentPosition() == expectedNormalized)
    }

    @Test
    fun `Division by 0 returns 0`() {
        every { browser.currentPosition } returns 10000.ms
        every { browser.duration } returns 0.ms
        val expectedNormalized = 0f

        assert(normalizeCurrentPosition() == expectedNormalized)
    }
}