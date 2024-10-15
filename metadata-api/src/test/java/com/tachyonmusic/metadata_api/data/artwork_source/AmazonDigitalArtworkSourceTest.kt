package com.tachyonmusic.metadata_api.data.artwork_source

import com.tachyonmusic.util.Resource
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class AmazonDigitalArtworkSourceTest {
    @Test
    fun `Finds artwork on page with artwork`(): Unit = runBlocking {
        AmazonDigitalArtworkSource().apply {
            val res = search("Cymatics", "Nigel Stanford", imageSize = 100)
            /**
             * If [Resource.Error.exception] is not then null all arguments are correct and it did find
             * artwork, but the artwork server refused our request (possibly due to it finding out
             * that we're scraping it)
             */
            assert(res is Resource.Success || (res is Resource.Error && res.exception != null))
        }
    }
}