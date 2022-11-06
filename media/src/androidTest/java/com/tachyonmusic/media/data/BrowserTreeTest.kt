package com.tachyonmusic.media.data

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.media.domain.model.TestSong
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class BrowserTreeTest {

    private lateinit var repository: TestUserRepository
    private lateinit var browserTree: BrowserTree

    @Before
    fun setUp() {
        repository = TestUserRepository()
        browserTree = BrowserTree(repository)

        val songs = MutableList(50) { i ->
            return@MutableList when {
                i < 10 -> {
                    val title = "Song $i of Artist 0"
                    val artist = "Artist 0"
                    TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                }
                i < 20 -> {
                    val title = "Song $i of Artist 1"
                    val artist = "Artist 1"
                    TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                }
                i < 30 -> {
                    val title = "Song $i of Artist 2"
                    val artist = "Artist 2"
                    TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                }
                else -> {
                    val title = "Song $i of Artist 3"
                    val artist = "Artist 3"
                    TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                }
            }
        }

        repository.complete(
            songs, listOf(), listOf()
        )
    }

    @Test
    fun getting_browser_tree_root_returns_all_playbacks() {
        runBlocking {
            val expectedItems =
                (repository.songs.value + repository.loops.value + repository.playlists.value).map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_songs_returns_all_songs() {
        runBlocking {
            val expectedItems = repository.songs.value.map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.SONG_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_loops_returns_all_loops() {
        runBlocking {
            val expectedItems = repository.loops.value.map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.LOOP_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_playlists_returns_all_playlists() {
        runBlocking {
            val expectedItems = repository.playlists.value.map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.PLAYLIST_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }
}





















