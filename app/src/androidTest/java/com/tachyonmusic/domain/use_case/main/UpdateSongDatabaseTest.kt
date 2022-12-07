package com.tachyonmusic.domain.use_case.main

import android.os.Environment
import com.daton.database.data.data_source.Database
import com.daton.database.data.repository.SettingsRepositoryImpl
import com.daton.database.data.repository.SongRepositoryImpl
import com.daton.database.di.DatabaseModule
import com.tachyonmusic.core.di.CoreModule
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.media.di.MediaPlaybackServiceModule
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.user.di.UserModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(
    DatabaseModule::class,
    UserModule::class,
    MediaPlaybackServiceModule::class,
    CoreModule::class
)
class UpdateSongDatabaseTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var fileRepository: FileRepository

    val excludedIndices = listOf(0, 3, 8, 12)

    val allFiles = fileRepository.getFilesInDirectoryWithExtensions(
        File(Environment.getExternalStorageDirectory().absolutePath + "/Music/"),
        listOf("mp3")
    )


    @Before
    fun setUp() {
        hiltRule.tryInject()
    }

    @After
    fun cleanUp() {
        database.clearAllTables()
    }

    @Test
    fun allSongsAreAddedToEmptyDatabaseWithNoExclusions(): Unit = runBlocking {
        val updateSongDatabase = UpdateSongDatabase(
            SongRepositoryImpl(database.songDao),
            SettingsRepositoryImpl(database.settingsDao),
            fileRepository
        )

        assert(database.songDao.getSongs().isEmpty())
        updateSongDatabase()
        assert(
            database.songDao.getSongs().size == fileRepository.getFilesInDirectoryWithExtensions(
                File(Environment.getExternalStorageDirectory().absolutePath + "/Music/"),
                listOf("mp3")
            ).size
        )
    }

    @Test
    fun allNotExcludedSongsAreAddedToEmptyDatabase(): Unit = runBlocking {
        val expectedSize = allFiles.size - excludedIndices.size
        val expectedFiles = mutableListOf<File>().apply { addAll(allFiles) }

        val updateSongDatabase = UpdateSongDatabase(
            SongRepositoryImpl(database.songDao),
            SettingsRepositoryImpl(database.settingsDao).apply {
                val toAdd = mutableListOf<String>()
                for (i in excludedIndices) {
                    toAdd += allFiles[i].absolutePath
                    expectedFiles -= allFiles[i]
                }


                addExcludedFilesRange(toAdd)
            },
            fileRepository
        )

        assert(database.songDao.getSongs().isEmpty())
        updateSongDatabase()
        assert(database.songDao.getSongs().size == expectedSize)
        assert(database.songDao.getSongs().map { it.mediaId.path!! }.containsAll(expectedFiles))
    }

    @Test
    fun newExclusionsAreRemovedFromPopulatedDatabase(): Unit = runBlocking {
        val settingsRepo = SettingsRepositoryImpl(database.settingsDao)

        val expectedFiles = mutableListOf<File>().apply {
            addAll(allFiles)
            for (i in excludedIndices)
                removeAt(i)
        }

        val updateSongDatabase = UpdateSongDatabase(
            SongRepositoryImpl(database.songDao),
            settingsRepo,
            fileRepository
        )
        updateSongDatabase()

        settingsRepo.addExcludedFilesRange(excludedIndices.map { allFiles[it].absolutePath })
        updateSongDatabase()

        assert(database.songDao.getSongs().size == expectedFiles.size)
        assert(database.songDao.getSongs().map { it.mediaId.path!! }.containsAll(expectedFiles))
    }

    @Test
    fun removingExclusionsAddsThemToDatabase(): Unit = runBlocking {
        val settingsRepo = SettingsRepositoryImpl(database.settingsDao)

        val expectedFiles = mutableListOf<File>().apply {
            addAll(allFiles)
            removeAt(excludedIndices[0])
        }

        val updateSongDatabase = UpdateSongDatabase(
            SongRepositoryImpl(database.songDao),
            settingsRepo.apply {
                val toAdd = mutableListOf<String>()
                for (i in excludedIndices) {
                    toAdd += allFiles[i].absolutePath
                }

                addExcludedFilesRange(toAdd)
            },
            fileRepository
        )
        updateSongDatabase()

        settingsRepo.removeExcludedFilesRange(excludedIndices.subList(1, excludedIndices.size).map { allFiles[it].absolutePath })
        updateSongDatabase()

        assert(database.songDao.getSongs().size == expectedFiles.size)
        assert(database.songDao.getSongs().map { it.mediaId.path!! }.containsAll(expectedFiles))
    }
}