package com.tachyonmusic.media.data

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.repository.RoomLoopRepository
import com.tachyonmusic.database.data.repository.RoomPlaylistRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.database.di.DatabaseModule
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toEntity
import com.tachyonmusic.media.domain.model.TestLoop
import com.tachyonmusic.media.domain.model.TestPlaylist
import com.tachyonmusic.media.domain.model.TestSong
import com.tachyonmusic.testutils.tryInject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(DatabaseModule::class)
@HiltAndroidTest
class BrowserTreeTest {
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
        val loops = MutableList(50) { i ->
            val name = "Loop $i"
            return@MutableList when {
                i < 10 -> {
                    val title = "Song $i of Artist 0"
                    val artist = "Artist 0"
                    TestLoop(
                        MediaId(artist + title),
                        name,
                        TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                    )
                }

                i < 20 -> {
                    val title = "Song $i of Artist 1"
                    val artist = "Artist 1"
                    TestLoop(
                        MediaId(artist + title),
                        name,
                        TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                    )
                }

                i < 30 -> {
                    val title = "Song $i of Artist 2"
                    val artist = "Artist 2"
                    TestLoop(
                        MediaId(artist + title),
                        name,
                        TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                    )
                }

                else -> {
                    val title = "Song $i of Artist 3"
                    val artist = "Artist 3"
                    TestLoop(
                        MediaId(artist + title),
                        name,
                        TestSong(MediaId(title + artist), title, artist, i.toLong() * 1000L)
                    )
                }
            }
        }
        val playlists = MutableList(50) { i ->
            val name = "Playlist $i"
            return@MutableList when {
                i < 10 -> {
                    val title = "Song $i of Artist 0"
                    val artist = "Artist 0"
                    TestPlaylist(
                        MediaId(title),
                        name,
                        List(10) {
                            TestSong(
                                MediaId(title + artist),
                                title,
                                artist,
                                i.toLong() * 1000L
                            ) as SinglePlayback
                        }.toMutableList().apply {
                            addAll(List(10) {
                                TestLoop(
                                    MediaId(artist + title),
                                    name,
                                    TestSong(
                                        MediaId(title + artist),
                                        title,
                                        artist,
                                        i.toLong() * 1000L
                                    )
                                ) as SinglePlayback
                            })
                        })
                }

                i < 20 -> {
                    val title = "Song $i of Artist 1"
                    val artist = "Artist 1"
                    TestPlaylist(
                        MediaId(title),
                        name,
                        List(10) {
                            TestSong(
                                MediaId(title + artist),
                                title,
                                artist,
                                i.toLong() * 1000L
                            ) as SinglePlayback
                        }.toMutableList().apply {
                            addAll(List(10) {
                                TestLoop(
                                    MediaId(artist + title),
                                    name,
                                    TestSong(
                                        MediaId(title + artist),
                                        title,
                                        artist,
                                        i.toLong() * 1000L
                                    )
                                ) as SinglePlayback
                            })
                        })
                }

                i < 30 -> {
                    val title = "Song $i of Artist 2"
                    val artist = "Artist 2"
                    TestPlaylist(
                        MediaId(title),
                        name,
                        List(10) {
                            TestSong(
                                MediaId(title + artist),
                                title,
                                artist,
                                i.toLong() * 1000L
                            ) as SinglePlayback
                        }.toMutableList().apply {
                            addAll(List(10) {
                                TestLoop(
                                    MediaId(artist + title),
                                    name,
                                    TestSong(
                                        MediaId(title + artist),
                                        title,
                                        artist,
                                        i.toLong() * 1000L
                                    )
                                ) as SinglePlayback
                            })
                        })
                }

                else -> {
                    val title = "Song $i of Artist 3"
                    val artist = "Artist 3"
                    TestPlaylist(
                        MediaId(title),
                        name,
                        List(10) {
                            TestSong(
                                MediaId(title + artist),
                                title,
                                artist,
                                i.toLong() * 1000L
                            ) as SinglePlayback
                        }.toMutableList().apply {
                            addAll(List(10) {
                                TestLoop(
                                    MediaId(artist + title),
                                    name,
                                    TestSong(
                                        MediaId(title + artist),
                                        title,
                                        artist,
                                        i.toLong() * 1000L
                                    )
                                ) as SinglePlayback
                            })
                        })
                }
            }
        }

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
