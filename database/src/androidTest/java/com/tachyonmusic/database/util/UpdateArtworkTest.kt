package com.tachyonmusic.database.util

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.di.DatabaseModule
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.testutils.tryInject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
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
                        0,
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
                        0,
                        listOf(),
                        artworkType = ArtworkType.NO_ARTWORK,
                        artworkUrl = null
                    ),
                    LoopEntity(
                        loopMediaId2,
                        "",
                        "",
                        0,
                        listOf(),
                        artworkType = ArtworkType.NO_ARTWORK,
                        artworkUrl = null
                    )
                )
            )
        }
    }

    @Test
    fun updatingSongEntityUpdatesAllOccurrencesOfArtwork(): Unit = runBlocking {
        val url = "ExampleUrl.com/example-search"

        val entityToEdit = songRepo.findByMediaId(songMediaId)!!

        for (artworkType in listOf(
            ArtworkType.NO_ARTWORK,
            ArtworkType.REMOTE,
            ArtworkType.EMBEDDED
        )) {
            assert(updateArtwork(songRepo, loopRepo, entityToEdit, artworkType, url))

            val databaseSong = songRepo.findByMediaId(songMediaId)!!
            assert(databaseSong.artworkType == artworkType)
            assert(databaseSong.artworkUrl == url)

            var databaseLoop = loopRepo.findByMediaId(loopMediaId1)!!
            assert(databaseLoop.artworkType == artworkType)
            assert(databaseLoop.artworkUrl == url)

            databaseLoop = loopRepo.findByMediaId(loopMediaId2)!!
            assert(databaseLoop.artworkType == artworkType)
            assert(databaseLoop.artworkUrl == url)
        }
    }


    @Test
    fun updatingLoopEntityUpdatesAllOccurrencesOfArtwork(): Unit = runBlocking {
        val url = "ExampleUrl.com/example-search"

        val entityToEdit = loopRepo.findByMediaId(loopMediaId2)!!
        for (artworkType in listOf(
            ArtworkType.NO_ARTWORK,
            ArtworkType.REMOTE,
            ArtworkType.EMBEDDED
        )) {
            assert(updateArtwork(songRepo, loopRepo, entityToEdit, artworkType, url))

            val databaseSong = songRepo.findByMediaId(songMediaId)!!
            assert(databaseSong.artworkType == artworkType)
            assert(databaseSong.artworkUrl == url)

            var databaseLoop = loopRepo.findByMediaId(loopMediaId1)!!
            assert(databaseLoop.artworkType == artworkType)
            assert(databaseLoop.artworkUrl == url)

            databaseLoop = loopRepo.findByMediaId(loopMediaId2)!!
            assert(databaseLoop.artworkType == artworkType)
            assert(databaseLoop.artworkUrl == url)
        }
    }

}