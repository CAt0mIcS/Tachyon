package com.tachyonmusic.media.data

import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.media.util.getCustomizedSongs
import com.tachyonmusic.media.util.getPlaylists
import com.tachyonmusic.media.util.getSongs
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class BrowserTreeTest {

    private val songRepository: SongRepository = mockk()
    private val customizedSongRepository: CustomizedSongRepository = mockk()
    private val playlistRepository: PlaylistRepository = mockk()
    private lateinit var browserTree: BrowserTree

    @Before
    fun setUp() {
        coEvery { songRepository.getSongs() } returns getSongs()
        coEvery { customizedSongRepository.getCustomizedSongs() } returns getCustomizedSongs()
        coEvery { playlistRepository.getPlaylists() } returns getPlaylists()

        browserTree = BrowserTree(songRepository, customizedSongRepository, playlistRepository)
    }

    @Test
    fun getting_browser_tree_root_returns_all_playbacks() = runTest {
        val expectedItems =
            (songRepository.getSongs() + customizedSongRepository.getCustomizedSongs() + playlistRepository.getPlaylists()).map { it.toMediaItem() }
        val items = browserTree.get(BrowserTree.ROOT, 0, expectedItems.size)
        assert(items?.containsAll(expectedItems) ?: false)
    }

    @Test
    fun getting_browser_tree_songs_returns_all_songs() = runTest {
        val expectedItems = songRepository.getSongs().map { it.toMediaItem() }
        val items = browserTree.get(BrowserTree.SONG_ROOT, 0, expectedItems.size)
        assert(items?.containsAll(expectedItems) ?: false)
    }

    @Test
    fun getting_browser_tree_customizedSongs_returns_all_customizedSongs() = runTest {
        val expectedItems = customizedSongRepository.getCustomizedSongs().map { it.toMediaItem() }
        val items = browserTree.get(BrowserTree.LOOP_ROOT, 0, expectedItems.size)
        assert(items?.containsAll(expectedItems) ?: false)
    }

    @Test
    fun getting_browser_tree_playlists_returns_all_playlists() = runTest {
        val expectedItems = playlistRepository.getPlaylists().map { it.toMediaItem() }
        val items = browserTree.get(BrowserTree.PLAYLIST_ROOT, 0, expectedItems.size)
        assert(items?.containsAll(expectedItems) ?: false)
    }

    // TODO: Does paging work?
}
