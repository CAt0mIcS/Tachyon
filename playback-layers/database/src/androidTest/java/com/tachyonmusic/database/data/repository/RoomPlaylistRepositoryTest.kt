package com.tachyonmusic.database.data.repository

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.tryInject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
internal class RoomPlaylistRepositoryTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: Database

    private lateinit var repository: RoomPlaylistRepository

    val playlistSize = 16

    @Before
    fun setUp() {
        hiltRule.tryInject()
        repository = RoomPlaylistRepository(database.playlistDao)

        runBlocking {
            repository.addAll(List(playlistSize) {
                PlaylistEntity(
                    MediaId.ofLocalPlaylist(it.toString()),
                    listOf()
                )
            })
        }
    }

    @Test
    fun hasPlaylistReturnsCorrectResult() = runTest {
        for (i in 0 until playlistSize) {
            assert(repository.findByMediaId(MediaId.ofLocalPlaylist(i.toString())) != null)
            assertEquals(repository.hasPlaylist(MediaId.ofLocalPlaylist(i.toString())), true)
        }

        assertEquals(repository.hasPlaylist(MediaId.EMPTY), false)
        assertEquals(repository.hasPlaylist(MediaId("jfklsda jifeljfids")), false)
        assertEquals(repository.hasPlaylist(MediaId.ofLocalPlaylist("324")), false)
    }
}