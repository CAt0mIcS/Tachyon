package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
internal class DeletePlaybackTest {

    val loopRepo = mockk<LoopRepository>()
    val playlistRepo = mockk<PlaylistRepository>()
    val historyRepo = mockk<HistoryRepository>()

    val deletePlayback = DeletePlayback(loopRepo, playlistRepo, historyRepo)
    val loopMediaId = MediaId.ofRemoteLoop("Loop", MediaId("Song"))
    val playlistMediaId = MediaId.ofRemotePlaylist("Playlist")

    val playlists = List(10) { i ->
        mockk<Playlist>().apply {
            every { mediaId } returns playlistMediaId
            every { hasPlayback(any()) } returns (i % 2 == 0)
            every { remove(any()) } answers {
                every { playbacks } returns emptyList()
            }
            every { playbacks } returns if (i % 2 == 0) listOf(
                mockk<Loop>().apply {
                    every { mediaId } returns loopMediaId
                },
                mockk<Song>().apply {
                    every { mediaId } returns MediaId("Song")
                }
            ) else emptyList()
        }
    }

    @Before
    fun setUp() {
        coEvery { playlistRepo.getPlaylists() } returns playlists

        coEvery { playlistRepo.setPlaybacksOfPlaylist(any(), any()) } returns Unit
        coEvery { playlistRepo.remove(any()) } returns Unit

        coEvery { historyRepo.removeHierarchical(any()) } returns Unit
        coEvery { loopRepo.remove(any()) } returns Unit
    }

    @Test
    fun `loop is deleted from any playlists containing it and from history`() =
        runTest {
            deletePlayback(mockk<Loop>().apply {
                every { mediaId } returns loopMediaId
            })

            coVerify { loopRepo.remove(loopMediaId) }

            playlists.forEach { playlist ->
                verify {
                    if (playlist.hasPlayback(any()))
                        playlist.remove(any())
                }
            }

            coVerify { playlistRepo.setPlaybacksOfPlaylist(playlistMediaId, emptyList()) }
            coVerify { historyRepo.removeHierarchical(loopMediaId) }
        }
}