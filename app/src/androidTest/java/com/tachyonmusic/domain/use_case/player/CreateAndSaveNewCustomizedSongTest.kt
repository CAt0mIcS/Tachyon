package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.LocalCustomizedSongImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.home.UpdateSongDatabase
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.File
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.getTestFiles
import com.tachyonmusic.util.ms
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
internal class CreateAndSaveNewCustomizedSongTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var customizedSongRepository: CustomizedSongRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val fileRepository: FileRepository = mockk()
    private val metadataExtractor: SongMetadataExtractor = mockk()

    private lateinit var updateSongDatabase: UpdateSongDatabase

    private lateinit var browser: MediaBrowserController
    val name = "TestCustomizedSong"

    private lateinit var createAndSaveNewCustomizedSong: CreateAndSaveNewCustomizedSong

    @Before
    fun setUp() {
        hiltRule.tryInject()

        every {
            fileRepository.getFilesInDirectoriesWithExtensions(
                any(),
                any()
            )
        } returns getTestFiles { File(it) }

        every { metadataExtractor.loadMetadata(any()) } answers {
            SongMetadataExtractor.SongMetadata("Title", "Artist", 10000.ms, firstArg())
        }
        every { metadataExtractor.loadBitmap(any()) } returns null

        browser = mockk()
        every { browser.playback } returns null
        every { browser.timingData } returns null
        every { browser.duration } returns null

        updateSongDatabase = UpdateSongDatabase(
            songRepository,
            settingsRepository,
            fileRepository,
            metadataExtractor
        )

        runTest {
            updateSongDatabase()
        }

        createAndSaveNewCustomizedSong = CreateAndSaveNewCustomizedSong(
            songRepository,
            customizedSongRepository,
            browser
        )

    }

    @Test
    fun nullPlaybackReturnsError() = runTest {
        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)
    }

    @Test
    fun invalidTimingDataReturnsError() = runTest {
        every { browser.playback } returns LocalSongImpl(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000.ms
        )

        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)

        every { browser.timingData } returns TimingDataController()
        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)

        every { browser.duration } returns 10000.ms
        every { browser.timingData } returns TimingDataController(
            listOf(TimingData(0.ms, 10000.ms))
        )
        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)

        every { browser.duration } returns 10000.ms
        every { browser.timingData } returns TimingDataController(
            listOf(
                TimingData(0.ms, 10000.ms),
                TimingData(0.ms, 10000.ms)
            )
        )
        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)
    }

    @Test
    fun timingDataWithOneStartingAtZeroAndEndingAtDurationAndOthersReturnsSuccess() =
        runTest {
            every { browser.playback } returns getSong()

            every { browser.duration } returns 10000.ms
            every { browser.timingData } returns TimingDataController(
                listOf(
                    TimingData(0.ms, 10000.ms),
                    TimingData(32.ms, 5634.ms)
                )
            )
            assert(createAndSaveNewCustomizedSong(name) is Resource.Success)
        }

    @Test
    fun invalidSongReturnsError() = runTest {
        every { browser.playback } returns LocalSongImpl(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000.ms
        )
        assert(createAndSaveNewCustomizedSong(name) is Resource.Error)
    }

    @Test
    fun correctSongReturnsCorrectCustomizedSong() = runTest {
        every { browser.playback } returns getSong()
        every { browser.timingData } returns TimingDataController(
            listOf(
                TimingData(0.ms, 323.ms),
                TimingData(443.ms, 6666.ms)
            )
        )

        val customizedSongRes = createAndSaveNewCustomizedSong(name)
        checkCustomizedSongResource(customizedSongRes)
    }

    @Test
    fun correctCustomizedSongReturnsCorrectCustomizedSong() = runTest {
        every { browser.playback } returns getCustomizedSong()
        every { browser.timingData } returns TimingDataController(
            listOf(
                TimingData(0.ms, 323.ms),
                TimingData(443.ms, 6666.ms)
            )
        )

        val customizedSongRes = createAndSaveNewCustomizedSong(name)
        checkCustomizedSongResource(customizedSongRes)
        val customizedSong = customizedSongRes.data!!
        assert(customizedSong.mediaId == MediaId.ofLocalCustomizedSong(name, getSong().mediaId))
    }


    private fun checkCustomizedSongResource(customizedSongRes: Resource<CustomizedSongEntity>) {
        assert(customizedSongRes is Resource.Success)
        assert(customizedSongRes.data != null)

        val customizedSong = customizedSongRes.data!!

        assert(customizedSong.songTitle == "Title")
        assert(customizedSong.songArtist == "Artist")
        assert(customizedSong.songDuration == 10000.ms)
        assert(customizedSong.timingData == browser.timingData?.timingData)
        assert(customizedSong.currentTimingDataIndex == browser.timingData?.currentIndex)
        // TODO: Check if artwork is correct
    }

    private fun getSong() = LocalSongImpl(
        MediaId.ofLocalSong(
            fileRepository.getFilesInDirectoriesWithExtensions(
                File(""),
                listOf()
            )[0]
        ), "Title", "Artist", 10000.ms
    )

    private fun getCustomizedSong(): LocalCustomizedSongImpl {
        val song = getSong()
        return LocalCustomizedSongImpl(
            MediaId.ofLocalCustomizedSong(name + "2", song.mediaId),
            name + "2",
            TimingDataController(listOf(TimingData(323.ms, 3233.ms))),
            song
        )
    }
}