package com.tachyonmusic.media.domain.use_case


import android.annotation.SuppressLint
import com.tachyonmusic.testutils.assertEquals
import org.junit.Before
import org.junit.Test

@SuppressLint("CheckResult")
internal class GetItemsOnPageWithPageSize {

    private val items = mutableListOf<Int>()

    companion object {
        const val SIZE = 10000
    }

    @Before
    fun setUp() {
        items.clear()
        for (i in (0 until SIZE)) {
            items.add(i)
        }
    }

    @Test
    fun `Getting page 0 with pageSize 30 returns the first 30 items`() {
        val page0Items = getItemsOnPageWithPageSize(items, 0, 30)
        assertEquals(page0Items.size, 30)
        assertEquals(page0Items, items.subList(0, 30))
    }

    @Test
    fun `Getting page x with pageSize 10 returns the xth 10 items`() {
        for (i in (0 until items.size)) {
            val page = getItemsOnPageWithPageSize(items, i, 10)
            if (i == SIZE)
                assertEquals(page.size, 10)
            else if (i * 10 + 10 < items.size)
                assertEquals(page, items.subList(i * 10, i * 10 + 10))
            else if (i * 10 < items.size)
                assertEquals(page, items.subList(i * 10, items.size))
            else
                assert(page.isEmpty())
        }
    }
}