package com.tachyonmusic.domain.use_case.player

import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.data.playback.RemoteLoopImpl
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.FileRepository
import com.tachyonmusic.domain.use_case.main.UpdateSongDatabase
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.TestMediaBrowserController
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
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
    lateinit var fileRepository: FileRepository

    @Inject
    lateinit var updateSongDatabase: UpdateSongDatabase

    lateinit var browser: TestMediaBrowserController
    val name = "TestLoop"

    private lateinit var createAndSaveNewLoop: CreateAndSaveNewLoop

    @Before
    fun setUp() {
        hiltRule.tryInject()

        runTest {
            updateSongDatabase()
        }

        browser = TestMediaBrowserController()
        createAndSaveNewLoop = CreateAndSaveNewLoop(
            songRepository,
            loopRepository,
            browser
        )

    }

    @Test
    fun nullPlaybackReturnsError() = runTest {
        browser.playback = null
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun invalidTimingDataReturnsError() = runTest {
        browser.playback = LocalSongImpl(MediaId("*0*DoesntExist.mp3"), "Title", "Artist", 10000L)

        browser.timingData = null
        assert(createAndSaveNewLoop(name) is Resource.Error)

        browser.timingData = mutableListOf()
        assert(createAndSaveNewLoop(name) is Resource.Error)

        browser.duration = 10000L
        browser.timingData = mutableListOf(TimingData(0L, 10000L))
        assert(createAndSaveNewLoop(name) is Resource.Error)

        browser.duration = 10000L
        browser.timingData = mutableListOf(TimingData(0L, 10000L), TimingData(0L, 10000L))
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun timingDataWithOneStartingAtZeroAndEndingAtDurationAndOthersReturnsSuccess() =
        runTest {
            browser.playback = getSong()

            browser.duration = 10000L
            browser.timingData = mutableListOf(TimingData(0L, 10000L), TimingData(32L, 5634L))
            assert(createAndSaveNewLoop(name) is Resource.Success)
        }

    @Test
    fun invalidSongReturnsError() = runTest {
        browser.playback = LocalSongImpl(MediaId("*0*DoesntExist.mp3"), "Title", "Artist", 10000L)
        assert(createAndSaveNewLoop(name) is Resource.Error)
    }

    @Test
    fun correctSongReturnsCorrectLoop() = runTest {
        browser.playback = getSong()
        browser.timingData = mutableListOf(TimingData(0, 323L), TimingData(443L, 6666L))

        val loopRes = createAndSaveNewLoop(name)
        checkLoopResource(loopRes)
    }

    @Test
    fun correctLoopReturnsCorrectLoop() = runTest {
        browser.playback = getLoop()
        browser.timingData = mutableListOf(TimingData(0, 323L), TimingData(443L, 6666L))

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