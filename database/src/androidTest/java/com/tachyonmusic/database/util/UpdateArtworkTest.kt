package com.tachyonmusic.database.util

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.ms
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
internal class UpdateArtworkTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var songRepo: SongRepository

    @Inject
    lateinit var loopRepo: LoopRepository

    val songMediaId = MediaId("test-song")
    val loopMediaId1 = MediaId("test-loop1", songMediaId)
    val loopMediaId2 = MediaId("test-loop2", songMediaId)

    @Before
    fun setUp() {
        hiltRule.tryInject()

        runBlocking {
            songRepo.addAll(
                listOf(
                    SongEntity(
                        songMediaId,
                        "",
                        "",
                        0.ms,
                        ArtworkType.NO_ARTWORK,
                        artworkUrl = null
                    )
                )
            )

            loopRepo.addAll(
                listOf(
                    LoopEntity(
                        loopMediaId1,
                        "",
                        "",
                        0.ms,
                        listOf()
                    ),
                    LoopEntity(
                        loopMediaId2,
                        "",
                        "",
                        0.ms,
                        listOf()
                    )
                )
            )
        }
    }

    @Test
    fun updatingSongEntityUpdatesArtwork(): Unit = runTest {
        val url = "ExampleUrl.com/example-search"

        val entityToEdit = songRepo.findByMediaId(songMediaId)!!

        for (artworkType in listOf(
            ArtworkType.NO_ARTWORK,
            ArtworkType.REMOTE,
            ArtworkType.EMBEDDED
        )) {
            assert(updateArtwork(songRepo, entityToEdit, artworkType, url))

            val databaseSong = songRepo.findByMediaId(songMediaId)!!
            assert(databaseSong.artworkType == artworkType)
            assert(databaseSong.artworkUrl == url)
        }
    }


    @Test
    fun updatingLoopEntityUpdatesSongEntitiesArtwork(): Unit = runTest {
        val url = "ExampleUrl.com/example-search"

        val entityToEdit = loopRepo.findByMediaId(loopMediaId2)!!
        for (artworkType in listOf(
            ArtworkType.NO_ARTWORK,
            ArtworkType.REMOTE,
            ArtworkType.EMBEDDED
        )) {
            assert(updateArtwork(songRepo, entityToEdit, artworkType, url))

            val databaseSong = songRepo.findByMediaId(songMediaId)!!
            assert(databaseSong.artworkType == artworkType)
            assert(databaseSong.artworkUrl == url)
        }
    }

}