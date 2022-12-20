package com.tachyonmusic.artworkfetcher

import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ArtworkFetcherTest {
    private lateinit var downloader: ArtworkFetcher

    @Before
    fun setUp() {
        downloader = ArtworkFetcher()
    }

    @Test
    fun `Invalid arguments, returns Resource Error`(): Unit = runBlocking {
        downloader.query("", "Artist", 100).map {
            assert(it is Resource.Loading || it is Resource.Error)
        }.collect()

        downloader.query("Title", "", 100).map {
            assert(it is Resource.Loading || it is Resource.Error)
        }.collect()

        downloader.query("Title", "Artist", 0).map {
            assert(it is Resource.Loading || it is Resource.Error)
        }.collect()
    }
}