package com.daton.artworkfetcher.data.artwork_source

import com.tachyonmusic.util.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AmazonDigitalArtworkSourceTest {
    @Test
    fun `Finds artwork on page with artwork`(): Unit = runBlocking {
        AmazonDigitalArtworkSource().apply {
            val url = getSearchUrl("Cymatics", "Nigel Stanford")
            assert(url.data != null)

            assert(executeSearch(url.data!!, 100) is Resource.Success)
        }
    }
}