package com.tachyonmusic.media.data

import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.repository.RoomLoopRepository
import com.tachyonmusic.database.data.repository.RoomPlaylistRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toEntity
import com.tachyonmusic.media.util.getLoops
import com.tachyonmusic.media.util.getPlaylists
import com.tachyonmusic.media.util.getSongs
import com.tachyonmusic.testutils.tryInject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
internal class BrowserTreeTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: Database

    private lateinit var songRepository: SongRepository
    private lateinit var loopRepository: LoopRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var browserTree: BrowserTree

    @Before
    fun setUp() {
        hiltRule.tryInject()
        songRepository = RoomSongRepository(database.songDao)
        loopRepository = RoomLoopRepository(database.loopDao)
        playlistRepository =
            RoomPlaylistRepository(database.playlistDao, songRepository, loopRepository)

        browserTree = BrowserTree(songRepository, loopRepository, playlistRepository)

        val songs = getSongs()
        val loops = getLoops()
        val playlists = getPlaylists()

        runBlocking {
            songRepository.addAll(songs.map { it.toEntity() })
            loopRepository.addAll(loops.map { it.toEntity() })
            playlistRepository.addAll(playlists.map { it.toEntity() })
        }
    }

    @Test
    fun getting_browser_tree_root_returns_all_playbacks() {
        runBlocking {
            val expectedItems =
                (songRepository.getSongs() + loopRepository.getLoops() + playlistRepository.getPlaylists()).map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_songs_returns_all_songs() {
        runBlocking {
            val expectedItems = songRepository.getSongs().map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.SONG_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_loops_returns_all_loops() {
        runBlocking {
            val expectedItems = loopRepository.getLoops().map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.LOOP_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    @Test
    fun getting_browser_tree_playlists_returns_all_playlists() {
        runBlocking {
            val expectedItems = playlistRepository.getPlaylists().map { it.toMediaItem() }
            val items = browserTree.get(BrowserTree.PLAYLIST_ROOT, 0, expectedItems.size)
            assert(items?.containsAll(expectedItems) ?: false)
        }
    }

    // TODO: Does paging work?
}
