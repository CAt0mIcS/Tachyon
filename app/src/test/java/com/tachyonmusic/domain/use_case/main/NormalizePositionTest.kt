package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.domain.repository.MediaBrowserController
import io.mockk.every
import io.mockk.mockk
import org.junit.Test


internal class NormalizePositionTest {

    private val browser: MediaBrowserController = mockk()
    private val normalizePosition = NormalizePosition(browser)

    @Test
    fun `Position returns correct normalized position`() {
        every { browser.currentPosition } returns 10000L
        every { browser.duration } returns 12568204L
        val expectedNormalized = browser.currentPosition!!.toFloat() / browser.duration!!.toFloat()

        assert(normalizePosition() == expectedNormalized)
    }

    @Test
    fun `Division by 0 returns 0`() {
        every { browser.currentPosition } returns 10000L
        every { browser.duration } returns 0L
        val expectedNormalized = 0f

        assert(normalizePosition() == expectedNormalized)
    }
}