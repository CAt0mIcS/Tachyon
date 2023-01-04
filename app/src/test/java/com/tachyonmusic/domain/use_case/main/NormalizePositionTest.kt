package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.util.TestMediaBrowserController
import org.junit.Test


internal class NormalizePositionTest {

    private val browser = TestMediaBrowserController()
    private val normalizePosition = NormalizePosition(browser)

    @Test
    fun `Position returns correct normalized position`() {
        browser.currentPosition = 10000L
        browser.duration = 12568204L
        val expectedNormalized = browser.currentPosition!!.toFloat() / browser.duration!!.toFloat()

        assert(normalizePosition() == expectedNormalized)
    }

    @Test
    fun `Division by 0 returns 0`() {
        browser.currentPosition = 10000L
        browser.duration = 0L
        val expectedNormalized = 0f

        assert(normalizePosition() == expectedNormalized)
    }
}