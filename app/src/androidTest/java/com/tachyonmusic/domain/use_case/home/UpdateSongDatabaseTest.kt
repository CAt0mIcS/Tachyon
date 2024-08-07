package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.repository.RoomSettingsRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.File
import com.tachyonmusic.util.getTestFiles
import com.tachyonmusic.util.ms
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
internal class UpdateSongDatabaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: Database

    private val excludedIndices = listOf(0, 3, 8, 12)
    private lateinit var allFiles: List<File>

    private val fileRepository: FileRepository = mockk()
    private val metadataExtractor: SongMetadataExtractor = mockk()


    @Before
    fun setUp() {
        hiltRule.tryInject()

        allFiles = getTestFiles {
            File(it)
        }

        every { metadataExtractor.loadMetadata(any()) } answers {
            SongMetadataExtractor.SongMetadata("Title", "Artist", 10000.ms, firstArg())
        }
        every { metadataExtractor.loadBitmap(any()) } returns null

        every {
            fileRepository.getFilesInDirectoriesWithExtensions(
                any(),
                any()
            )
        } returns allFiles
    }

    @After
    fun cleanUp() {
        database.clearAllTables()
    }

    @Test
    fun allSongsAreAddedToEmptyDatabaseWithNoExclusions() = runTest {
        val updateSongDatabase = UpdateSongDatabase(
            RoomSongRepository(database.songDao),
            RoomSettingsRepository(database.settingsDao),
            fileRepository,
            metadataExtractor
        )

        assert(database.songDao.getSongs().isEmpty())
        updateSongDatabase()
        assertEquals(database.songDao.getSongs().size, allFiles.size)
    }

    @Test
    fun allNotExcludedSongsAreAddedToEmptyDatabase() = runTest {
        val expectedSize = allFiles.size - excludedIndices.size
        val expectedFiles = mutableListOf<File>().apply { addAll(allFiles) }

        val updateSongDatabase = UpdateSongDatabase(
            RoomSongRepository(database.songDao),
            RoomSettingsRepository(database.settingsDao).apply {
                val toAdd = mutableListOf<String>()
                for (i in excludedIndices) {
                    toAdd += allFiles[i].absolutePath
                    expectedFiles -= allFiles[i]
                }


                addExcludedFilesRange(toAdd)
            },
            fileRepository,
            metadataExtractor
        )

        assert(database.songDao.getSongs().isEmpty())
        updateSongDatabase()
        assertEquals(database.songDao.getSongs().size, expectedSize)

        val songs = database.songDao.getSongs().map { it.mediaId.uri!!.absolutePath }
        assert(songs.containsAll(expectedFiles.map { it.absolutePath }))
    }


    @Test
    fun newExclusionsAreRemovedFromPopulatedDatabase() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().isFile } returns true

        val settingsRepo = RoomSettingsRepository(database.settingsDao)

        val expectedFiles = mutableListOf<File>().apply {
            addAll(allFiles)
        }
        for (i in excludedIndices.size - 1 downTo 0)
            expectedFiles.removeAt(excludedIndices[i])

        val updateSongDatabase = UpdateSongDatabase(
            RoomSongRepository(database.songDao),
            settingsRepo,
            fileRepository,
            metadataExtractor
        )
        updateSongDatabase()

        settingsRepo.addExcludedFilesRange(excludedIndices.map { allFiles[it].absolutePath })
        updateSongDatabase()

        assertEquals(database.songDao.getSongs().size, expectedFiles.size)
        val databaseSongs = database.songDao.getSongs().map { it.mediaId.uri!! }
        assertEquals(databaseSongs.map { it.absolutePath }, expectedFiles.map { it.absolutePath })
    }

    @Test
    fun removingExclusionsAddsThemToDatabase() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().isFile } returns true

        val settingsRepo = RoomSettingsRepository(database.settingsDao)

        val expectedFiles = mutableListOf<File>().apply {
            addAll(allFiles)
            removeAt(excludedIndices[0])
        }

        val updateSongDatabase = UpdateSongDatabase(
            RoomSongRepository(database.songDao),
            settingsRepo.apply {
                val toAdd = mutableListOf<String>()
                for (i in excludedIndices) {
                    toAdd += allFiles[i].absolutePath
                }

                addExcludedFilesRange(toAdd)
            },
            fileRepository,
            metadataExtractor
        )
        updateSongDatabase()

        settingsRepo.removeExcludedFilesRange(
            excludedIndices.subList(1, excludedIndices.size).map { allFiles[it].absolutePath })
        updateSongDatabase()

        assertEquals(database.songDao.getSongs().size, expectedFiles.size)
        assert(database.songDao.getSongs().map { it.mediaId.uri!! }.containsAll(expectedFiles))
    }

    // TODO: More possible tests
}