package com.tachyonmusic.user.data.repository

import android.annotation.SuppressLint
import androidx.test.platform.app.InstrumentationRegistry
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.data.playback.RemoteLoop
import com.tachyonmusic.core.data.playback.RemotePlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.user.di.AppModule
import com.tachyonmusic.user.domain.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
        hiltRule.inject()

        runBlocking {
            loops = MutableList(3) { i ->
                RemoteLoop(
                    MediaId.ofRemoteLoop(i.toString(), MediaId(i.toString())),
                    i.toString(),
                    arrayListOf(TimingData(1, 10), TimingData(100, 1000)),
                    repository.songs.await().find { it.title == "Title:$i" }!!
                ) as Loop
            } as ArrayList

            playlists = MutableList(2) { i ->
                RemotePlaylist(
                    MediaId.ofRemotePlaylist(i.toString()),
                    i.toString(),
                    repository.songs.await().filter {
                        it.title == "Title:0" || it.title == "Title:3" || it.title == "Title:7"
                    } as MutableList<SinglePlayback>
                ) as Playlist
            } as ArrayList
        }
    }

    @After
    fun cleanUp() = runBlocking {
        if (repository.signedIn) {
            assertResource(repository.delete())
        }
    }

    @Test
    fun test(): Unit = runBlocking {
        for (loop in loops)
            repository += loop
        for (playlist in playlists)
            repository += playlist

        assertResource(repository.upload())

        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))

        assertEquals(repository.loops.await(), loops)
        assertEquals(repository.playlists.await(), playlists)
    }

    private fun <T> assertResource(res: Resource<T>) {
        assert(res is Resource.Success) { "${res.message?.asString(InstrumentationRegistry.getInstrumentation().targetContext)}" }
    }
}