package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


internal class DeletePlaybackTest {

    val remixRepo = mockk<RemixRepository>()
    val playlistRepo = mockk<PlaylistRepository>()
    val historyRepo = mockk<HistoryRepository>()

    val deletePlayback = DeletePlayback(remixRepo, playlistRepo, historyRepo)
    val remixMediaId = MediaId.ofLocalRemix("CustomizedSong", MediaId("Song"))
    val playlistMediaId = MediaId.ofLocalPlaylist("Playlist")

    val playlists = List(10) { i ->
        mockk<Playlist>().apply {
            every { mediaId } returns playlistMediaId
            every { hasPlayback(any()) } returns (i % 2 == 0)
            every { remove(any()) } answers {
                every { playbacks } returns emptyList()
            }
            every { playbacks } returns if (i % 2 == 0) listOf(
                mockk<Remix>().apply {
                    every { mediaId } returns remixMediaId
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
        coEvery { remixRepo.remove(any()) } returns Unit
    }

    @Test
    fun `remix is deleted from any playlists containing it and from history`() =
        runTest {
            deletePlayback(mockk<Remix>().apply {
                every { mediaId } returns remixMediaId
            })

            coVerify { remixRepo.remove(remixMediaId) }

            playlists.forEach { playlist ->
                verify {
                    if (playlist.hasPlayback(any()))
                        playlist.remove(any())
                }
            }

            coVerify { playlistRepo.setPlaybacksOfPlaylist(playlistMediaId, emptyList()) }
            coVerify { historyRepo.removeHierarchical(remixMediaId) }
        }
}