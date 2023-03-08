package com.tachyonmusic.media.data

import android.graphics.Bitmap
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.artwork.data.ArtworkLoaderImpl
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.assertResource
import com.tachyonmusic.util.File
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.ms
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.net.URI


@Suppress("TestFunctionName")
@OptIn(ExperimentalCoroutinesApi::class)
internal class ArtworkLoaderImplTest {
    private val log: Logger = mockk(relaxed = true)
    private val artworkFetcher: ArtworkFetcher = mockk()
    private val mediaId: MediaId = mockk(relaxed = true)
    private val metadataExtractor: SongMetadataExtractor = mockk()
    private val artworkLoader =
        com.tachyonmusic.artwork.data.ArtworkLoaderImpl(artworkFetcher, log, metadataExtractor)

    @Test
    fun NO_ARTWORK_ReturnsCorrectResource() = runTest {
        val res = artworkLoader.requestLoad(getEntity(ArtworkType.NO_ARTWORK))
        assertResource(res)
        assertEquals(res.data!!.artwork, null)
        assertEquals(res.data!!.entityToUpdate, null)
    }

    @Test
    fun EMBEDDED_ReturnsCorrectResource() = runTest {
        every { mediaId.uri } returns null

        var res = artworkLoader.requestLoad(getEntity(ArtworkType.EMBEDDED))
        assert(res is Resource.Error)
        assertEquals(res.data!!.artwork, null)
        assertEquals(res.data!!.entityToUpdate!!.artworkType, ArtworkType.UNKNOWN)


        every { mediaId.uri } returns File("SomePath.mp3")
        every { metadataExtractor.loadBitmap(any()) } returns null

        res = artworkLoader.requestLoad(getEntity(ArtworkType.EMBEDDED))
        assert(res is Resource.Error)
        assertEquals(res.data, null)

        val bitmap: Bitmap = mockk()
        val path = File("SomePath.mp3")
        every { mediaId.uri } returns path
        every { metadataExtractor.loadBitmap(any()) } returns bitmap

        res = artworkLoader.requestLoad(getEntity(ArtworkType.EMBEDDED))
        assertResource(res)
        assertEquals(res.data!!.artwork, EmbeddedArtwork(bitmap, path))
        assertEquals(res.data!!.entityToUpdate, null)
    }

    @Test
    fun REMOTE_ReturnsCorrectResource() = runTest {
        var res =
            artworkLoader.requestLoad(getEntity(ArtworkType.REMOTE, artworkUrl = "    \n \t "))
        assert(res is Resource.Error)
        assertEquals(res.data!!.entityToUpdate!!.artworkType, ArtworkType.UNKNOWN)
        assertEquals(res.data!!.entityToUpdate!!.artworkUrl, null)


        res = artworkLoader.requestLoad(getEntity(ArtworkType.REMOTE, "www.example.com"))
        assertResource(res)
        assertEquals(res.data!!.artwork, RemoteArtwork(URI("www.example.com")))
        assertEquals(res.data!!.entityToUpdate, null)
    }

    @Test
    fun UNKNOWN_ReturnsCorrectResource() = runTest {
        coEvery {
            artworkFetcher.query(any(), any(), any())
        } returns flow {
            emit(Resource.Error())
        }
        every { mediaId.uri } returns null

        var res = artworkLoader.requestLoad(getEntity(ArtworkType.UNKNOWN))
        assert(res is Resource.Error)
        assertEquals(res.data!!.artwork, null)
        assertEquals(res.data!!.entityToUpdate!!.artworkType, ArtworkType.NO_ARTWORK)


        val bitmap: Bitmap = mockk()
        val file = File("SomeSong.mp3")
        every { mediaId.uri } returns file
        every { metadataExtractor.loadBitmap(any()) } returns bitmap

        res = artworkLoader.requestLoad(getEntity(ArtworkType.UNKNOWN))
        assertResource(res)
        assertEquals(res.data!!.artwork, EmbeddedArtwork(bitmap, file))
        assertEquals(res.data!!.entityToUpdate!!.artworkType, ArtworkType.EMBEDDED)


        val url = "https://www.example.com/SomeImage.jpg"
        every { mediaId.uri } returns null
        coEvery {
            artworkFetcher.query(any(), any(), any())
        } returns flow {
            emit(Resource.Success(url))
        }

        res = artworkLoader.requestLoad(getEntity(ArtworkType.UNKNOWN))
        assertResource(res)
        assertEquals(res.data!!.artwork, RemoteArtwork(URI(url)))
        assertEquals(res.data!!.entityToUpdate!!.artworkType, ArtworkType.REMOTE)
    }


    private fun getEntity(artworkType: String, artworkUrl: String? = null) = SongEntity(
        mediaId,
        "SomeTitle",
        "SomeArtist",
        duration = 10000.ms,
        artworkType,
        artworkUrl
    )
}