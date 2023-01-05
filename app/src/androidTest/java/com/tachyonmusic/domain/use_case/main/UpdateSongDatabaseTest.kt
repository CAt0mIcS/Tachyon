package com.tachyonmusic.domain.use_case.main

import com.tachyonmusic.database.data.data_source.Database
import com.tachyonmusic.database.data.repository.RoomSettingsRepository
import com.tachyonmusic.database.data.repository.RoomSongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.File
import com.tachyonmusic.util.TestSongMetadataExtractor
import com.tachyonmusic.util.getTestFiles
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
internal class UpdateSongDatabaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: Database

    val excludedIndices = listOf(0, 3, 8, 12)

    lateinit var allFiles: List<File>

    val fileRepository: FileRepository = mockk()


    @Before
    fun setUp() {
        hiltRule.tryInject()

        allFiles = getTestFiles {
            File(it)
        }

        every {
            fileRepository.getFilesInDirectoryWithExtensions(
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
            TestSongMetadataExtractor()
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
            TestSongMetadataExtractor()
        )

        assert(database.songDao.getSongs().isEmpty())
        updateSongDatabase()
        assertEquals(database.songDao.getSongs().size, expectedSize)

        val songs = database.songDao.getSongs().map { it.mediaId.path!!.absolutePath }
        assert(songs.containsAll(expectedFiles.map { it.absolutePath }))
    }

    // TODO: Tests fail due to [File.exists] check in [UpdateSongDatabase]
    //   create temporary files to bypass check
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
            TestSongMetadataExtractor()
        )
        updateSongDatabase()

        settingsRepo.addExcludedFilesRange(excludedIndices.map { allFiles[it].absolutePath })
        updateSongDatabase()

        assertEquals(database.songDao.getSongs().size, expectedFiles.size)
        val databaseSongs = database.songDao.getSongs().map { it.mediaId.path!! }
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
            TestSongMetadataExtractor()
        )
        updateSongDatabase()

        settingsRepo.removeExcludedFilesRange(
            excludedIndices.subList(1, excludedIndices.size).map { allFiles[it].absolutePath })
        updateSongDatabase()

        assertEquals(database.songDao.getSongs().size, expectedFiles.size)
        assert(database.songDao.getSongs().map { it.mediaId.path!! }.containsAll(expectedFiles))
    }

    // TODO: More possible tests
}