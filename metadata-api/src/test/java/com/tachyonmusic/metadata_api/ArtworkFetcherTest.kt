package com.tachyonmusic.metadata_api

import com.tachyonmusic.metadata_api.domain.artwork_source.ArtworkSource
import com.tachyonmusic.util.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class ArtworkFetcherTest {
    private lateinit var downloader: ArtworkFetcher

    @Before
    fun setUp() {
        downloader = ArtworkFetcher()
    }

    @Test
    fun `Invalid arguments, returns Resource Error`(): Unit = runTest {
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

    @Test
    fun `Valid arguments, returns Resource Success`() = runTest {
        val source = mockk<ArtworkSource>()
        every { source.search(any(), any(), any()) } returns Resource.Success()

        ArtworkFetcher(listOf(source)).query("Title", "Artist", 100).map {
            assert(it is Resource.Loading || it is Resource.Success)
        }.collect()
    }
}