package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class NormalizeCurrentPositionTest {

    private val browser: MediaBrowserController = mockk()
    private val normalizeCurrentPosition = NormalizeCurrentPosition(browser)

    @Test
    fun `Position returns correct normalized position`() {
        every { browser.currentPosition } returns 10000L
        every { browser.duration } returns 12568204L
        val expectedNormalized = browser.currentPosition!!.toFloat() / browser.duration!!.toFloat()

        assert(normalizeCurrentPosition() == expectedNormalized)
    }

    @Test
    fun `Division by 0 returns 0`() {
        every { browser.currentPosition } returns 10000L
        every { browser.duration } returns 0L
        val expectedNormalized = 0f

        assert(normalizeCurrentPosition() == expectedNormalized)
    }
}