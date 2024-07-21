package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class AddSongToExcludedSongsTest {

    val settingsRepo = mockk<SettingsRepository>()
    val songRepo = mockk<SongRepository>()
    val historyRepo = mockk<HistoryRepository>()
    val remixRepo = mockk<RemixRepository>()
    val playlistRepo = mockk<PlaylistRepository>()

    val song = mockk<Song>().apply {
        every { uri } returns mockk()
    }

    val exclude =
        AddSongToExcludedSongs(settingsRepo, songRepo, historyRepo, remixRepo, playlistRepo)

    val playlists = List(10) { i ->
        mockk<Playlist>().apply {
            every { mediaId } returns MediaId.EMPTY
            every { hasPlayback(any()) } returns (i % 2 == 0)
            every { remove(any()) } answers {
                every { playbacks } returns emptyList()
            }
            every { playbacks } returns if (i % 2 == 0) listOf(mockk<Song>().apply {
                every { mediaId } returns MediaId.EMPTY
            }) else emptyList()
        }
    }


    @Before
    fun setUp() {
        coEvery { settingsRepo.addExcludedFilesRange(any()) } returns Unit
        coEvery { songRepo.remove(song.mediaId) } returns Unit
        coEvery { historyRepo.removeHierarchical(song.mediaId) } returns Unit
        coEvery { remixRepo.removeIf(any()) } returns Unit
        coEvery { playlistRepo.getPlaylists() } returns playlists
        coEvery { playlistRepo.setPlaybacksOfPlaylist(MediaId.EMPTY, emptyList()) } returns Unit
    }


    @Test
    fun test() = runTest {
        exclude(song)

        coVerify { settingsRepo.addExcludedFilesRange(any()) }
        coVerify { songRepo.remove(song.mediaId) }
        coVerify { historyRepo.removeHierarchical(song.mediaId) }
        coVerify { remixRepo.removeIf(any()) }

        playlists.forEach { playlist ->
            verify {
                if (playlist.hasPlayback(song))
                    playlist.remove(song)
            }
        }

        coVerify { playlistRepo.setPlaybacksOfPlaylist(any(), emptyList()) }
    }
}