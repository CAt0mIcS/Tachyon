package com.tachyonmusic.media.data

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.assertResource
import com.tachyonmusic.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ArtworkCodexImplTest {

    private val artworkLoader: com.tachyonmusic.artwork.domain.ArtworkLoader = mockk()
    private val log: Logger = mockk(relaxed = true)
    private val codex = com.tachyonmusic.artwork.data.ArtworkCodexImpl(artworkLoader, log)

    private val testEntity = SinglePlaybackEntity(
        MediaId("/Music/SomeSong.mp3"),
        "SomeTitle",
        "SomeArtist",
        duration = 10000L,
        artworkType = ArtworkType.UNKNOWN,
        artworkUrl = null
    )

    @Before
    fun setUp() {
        coEvery { artworkLoader.requestLoad(testEntity) } returns Resource.Success(com.tachyonmusic.artwork.domain.ArtworkLoader.ArtworkData())
    }

    @Test
    fun `awaitOrLoad calls requestLoad for non-existent artwork`() = runTest {
        codex.awaitOrLoad(testEntity)
        coVerify { artworkLoader.requestLoad(testEntity) }
    }

    @Test
    fun `awaitOrLoad returns success for already loaded artwork`() = runTest {
        // Add new artwork
        codex.awaitOrLoad(testEntity)

        val res = codex.awaitOrLoad(testEntity)
        assertResource(res)
        assertEquals(res.data, null)
    }

    @Test
    fun `new awaitOrLoad calls wait for ongoing`() = runTest {
        coEvery { artworkLoader.requestLoad(testEntity) } coAnswers {
            delay(10000)
            Resource.Success(
                com.tachyonmusic.artwork.domain.ArtworkLoader.ArtworkData(
                    entityToUpdate = SinglePlaybackEntity(
                        testEntity.mediaId,
                        testEntity.title,
                        testEntity.artist,
                        testEntity.duration,
                        ArtworkType.NO_ARTWORK,
                        null
                    )
                )
            )
        }

        val job1 = async { codex.awaitOrLoad(testEntity) }
        val job2 = async { codex.awaitOrLoad(testEntity) }
        val job3 = async { codex.awaitOrLoad(testEntity) }

        assertResource(job1.await())
        assertResource(job2.await())
        assertResource(job3.await())

        coVerify(exactly = 1) { artworkLoader.requestLoad(testEntity) }
        verify(exactly = 2) { log.debug("Waiting for artwork job ${testEntity.mediaId} to join...") }
        verify(exactly = 2) { log.debug("Artwork job ${testEntity.mediaId} finished") }
    }

    @Test
    fun `isLoaded returns correct result single thread`() = runTest {
        assert(!codex.isLoaded(testEntity.mediaId))
        codex.awaitOrLoad(testEntity)
        assert(codex.isLoaded(testEntity.mediaId))
    }

    @Test
    fun `isLoaded returns correct result multiple threads`() = runTest {
        assert(!codex.isLoaded(testEntity.mediaId))

        val job1 = async { codex.awaitOrLoad(testEntity) }
        val job2 = async { codex.awaitOrLoad(testEntity) }
        val job3 = async { codex.awaitOrLoad(testEntity) }

        assert(!codex.isLoaded(testEntity.mediaId))

        job1.join()
        assert(codex.isLoaded(testEntity.mediaId))

        job2.join()
        job3.join()
        assert(codex.isLoaded(testEntity.mediaId))
    }
}