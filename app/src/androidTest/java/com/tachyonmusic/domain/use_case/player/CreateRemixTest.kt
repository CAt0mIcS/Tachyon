package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.LocalRemix
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.repository.RemixRepository
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@HiltAndroidTest
internal class CreateRemixTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var remixRepository: RemixRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val fileRepository: FileRepository = mockk()
    private val metadataExtractor: SongMetadataExtractor = mockk()

    private lateinit var updateSongDatabase: UpdateSongDatabase

    private lateinit var browser: MediaBrowserController
    val name = "TestCustomizedSong"

    private lateinit var createRemix: CreateRemix

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

        createRemix = CreateRemix(
            songRepository,
            remixRepository,
            browser
        )

    }

    @Test
    fun nullPlaybackReturnsError() = runTest {
        assert(createRemix(name) is Resource.Error)
    }

    @Test
    fun invalidTimingDataReturnsError() = runTest {
        every { browser.playback } returns LocalSong(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000.ms
        )

        assert(createRemix(name) is Resource.Error)

        every { browser.timingData } returns TimingDataController()
        assert(createRemix(name) is Resource.Error)

        every { browser.duration } returns 10000.ms
        every { browser.timingData } returns TimingDataController(
            listOf(TimingData(0.ms, 10000.ms))
        )
        assert(createRemix(name) is Resource.Error)

        every { browser.duration } returns 10000.ms
        every { browser.timingData } returns TimingDataController(
            listOf(
                TimingData(0.ms, 10000.ms),
                TimingData(0.ms, 10000.ms)
            )
        )
        assert(createRemix(name) is Resource.Error)
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
            assert(createRemix(name) is Resource.Success)
        }

    @Test
    fun invalidSongReturnsError() = runTest {
        every { browser.playback } returns LocalSong(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000.ms
        )
        assert(createRemix(name) is Resource.Error)
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

        val remixRes = createRemix(name)
        checkCustomizedSongResource(remixRes)
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

        val remixRes = createRemix(name)
        checkCustomizedSongResource(remixRes)
        val remix = remixRes.data!!
        assert(remix.mediaId == MediaId.ofLocalRemix(name, getSong().mediaId))
    }


    private fun checkCustomizedSongResource(remixRes: Resource<RemixEntity>) {
        assert(remixRes is Resource.Success)
        assert(remixRes.data != null)

        val remix = remixRes.data!!

        assert(remix.songTitle == "Title")
        assert(remix.songArtist == "Artist")
        assert(remix.songDuration == 10000.ms)
        assert(remix.timingData == browser.timingData?.timingData)
        assert(remix.currentTimingDataIndex == browser.timingData?.currentIndex)
        // TODO: Check if artwork is correct
    }

    private fun getSong() = LocalSong(
        MediaId.ofLocalSong(
            fileRepository.getFilesInDirectoriesWithExtensions(
                File(""),
                listOf()
            )[0]
        ), "Title", "Artist", 10000.ms
    )

    private fun getCustomizedSong(): LocalRemix {
        val song = getSong()
        return LocalRemix(
            MediaId.ofLocalRemix(name + "2", song.mediaId),
            name + "2",
            TimingDataController(listOf(TimingData(323.ms, 3233.ms))),
            song
        )
    }
}