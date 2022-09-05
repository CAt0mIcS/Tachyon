package com.tachyonmusic.user.data.repository

import android.annotation.SuppressLint
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.ktx.Firebase
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.data.playback.RemotePlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.user.data.Metadata
import com.tachyonmusic.user.di.AppModule
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.assertEquals
import com.tachyonmusic.util.assertResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.IllegalStateException
import javax.inject.Inject

@SuppressLint("CheckResult")
@HiltAndroidTest
@UninstallModules(AppModule::class)
class FirebaseRepositoryTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    companion object {
        const val TEST_EMAIL = "test1@test.com"
        const val TEST_PASSWORD = "testPassword"
    }

    @Inject
    lateinit var repository: UserRepository

    private lateinit var loops: ArrayList<Loop>
    private lateinit var playlists: ArrayList<Playlist>

    @Before
    fun setUp() {
        tryInject()

        runBlocking {
            loops = MutableList(3) { i ->
                val song = repository.songs.await()[i]

                RemoteLoop(
                    MediaId.ofRemoteLoop(i.toString(), song.mediaId),
                    i.toString(),
                    arrayListOf(TimingData(1, 10), TimingData(100, 1000)),
                    song
                ) as Loop
            } as ArrayList

            playlists = MutableList(2) { i ->
                RemotePlaylist(
                    MediaId.ofRemotePlaylist(i.toString()),
                    i.toString(),
                    repository.songs.await().filter {
                        it.title == "Cosmic Storm" || it.title == "Awake" || it.title == "Last Time"
                    } as MutableList<SinglePlayback>
                ) as Playlist
            } as ArrayList

            cleanUp()
        }
    }

    @After
    fun cleanUp() = runBlocking {
        tryInject()
        if (repository.signedIn) {
            (repository as FirebaseRepository).localCache.set(Metadata())
            assertResource(repository.delete())
        }
    }

    /**
     * Completely new user first uploads loops and playlists, while not being registered yet.
     * Then registers new user
     * --> Check that the repository-stored loops and playlists match the ones previously uploaded
     */
    @Test
    fun unregisteredUploadRegisteredDownload(): Unit =
        runBlocking {
            for (loop in loops)
                repository += loop
            for (playlist in playlists)
                repository += playlist

            assertResource(repository.upload())

            assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))

            assertEquals(repository.loops.await(), loops)
            assertEquals(repository.playlists.await(), playlists)
        }

    private fun tryInject() = try {
        hiltRule.inject()
    } catch (e: IllegalStateException) {
    }
}