package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.SongMetadataExtractor
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.File
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.getTestFiles
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
internal class CreateAndSaveNewLoopTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var songRepository: SongRepository

    @Inject
    lateinit var loopRepository: LoopRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val fileRepository: FileRepository = mockk()
    private val metadataExtractor: SongMetadataExtractor = mockk()

    private lateinit var updateSongDatabase: UpdateSongDatabase

    private lateinit var browser: MediaBrowserController
    val name = "TestLoop"

    private lateinit var createAndSaveNewLoop: CreateAndSaveNewLoop

    @Before
    fun setUp() {
        hiltRule.tryInject()

        every {
            fileRepository.getFilesInDirectoryWithExtensions(
                any(),
                any()
            )
        } returns getTestFiles { File(it) }

        every { metadataExtractor.loadMetadata(any()) } answers {
            SongMetadataExtractor.SongMetadata("Title", "Artist", 10000L, firstArg())
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

        createAndSaveNewLoop = CreateAndSaveNewLoop(
            songRepository,
            loopRepository,
            browser
        )

    }

    @Test
    fun nullPlaybackReturnsError() = runTest {
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun invalidTimingDataReturnsError() = runTest {
        every { browser.playback } returns LocalSongImpl(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000L
        )

        assert(createAndSaveNewLoop(name) is Resource.Error)

        every { browser.timingData } returns mutableListOf()
        assert(createAndSaveNewLoop(name) is Resource.Error)

        every { browser.duration } returns 10000L
        every { browser.timingData } returns mutableListOf(TimingData(0L, 10000L))
        assert(createAndSaveNewLoop(name) is Resource.Error)

        every { browser.duration } returns 10000L
        every { browser.timingData } returns mutableListOf(
            TimingData(0L, 10000L),
            TimingData(0L, 10000L)
        )
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun timingDataWithOneStartingAtZeroAndEndingAtDurationAndOthersReturnsSuccess() =
        runTest {
            every { browser.playback } returns getSong()

            every { browser.duration } returns 10000L
            every { browser.timingData } returns mutableListOf(
                TimingData(0L, 10000L),
                TimingData(32L, 5634L)
            )
            assert(createAndSaveNewLoop(name) is Resource.Success)
        }

    @Test
    fun invalidSongReturnsError() = runTest {
        every { browser.playback } returns LocalSongImpl(
            MediaId("*0*DoesntExist.mp3"),
            "Title",
            "Artist",
            10000L
        )
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun correctSongReturnsCorrectLoop() = runTest {
        every { browser.playback } returns getSong()
        every { browser.timingData } returns mutableListOf(
            TimingData(0, 323L),
            TimingData(443L, 6666L)
        )

        val loopRes = createAndSaveNewLoop(name)
        checkLoopResource(loopRes)
    }

    @Test
    fun correctLoopReturnsCorrectLoop() = runTest {
        every { browser.playback } returns getLoop()
        every { browser.timingData } returns mutableListOf(
            TimingData(0, 323L),
            TimingData(443L, 6666L)
        )

        val loopRes = createAndSaveNewLoop(name)
        checkLoopResource(loopRes)
        val loop = loopRes.data!!
        assert(loop.mediaId == MediaId.ofRemoteLoop(name, getSong().mediaId))
    }


    private fun checkLoopResource(loopRes: Resource<LoopEntity>) {
        assert(loopRes is Resource.Success)
        assert(loopRes.data != null)

        val loop = loopRes.data!!

        assert(loop.songTitle == "Title")
        assert(loop.songArtist == "Artist")
        assert(loop.songDuration == 10000L)
        assert(loop.timingData == browser.timingData)
        // TODO: Check if artwork is correct
    }

    private fun getSong() = LocalSongImpl(
        MediaId.ofLocalSong(
            fileRepository.getFilesInDirectoryWithExtensions(
                File(""),
                listOf()
            )[0]
        ), "Title", "Artist", 10000L
    )

    private fun getLoop(): RemoteLoopImpl {
        val song = getSong()
        return RemoteLoopImpl(
            MediaId.ofRemoteLoop(name + "2", song.mediaId),
            name + "2",
            TimingDataController(listOf(TimingData(323L, 3233L))),
            song
        )
    }
}