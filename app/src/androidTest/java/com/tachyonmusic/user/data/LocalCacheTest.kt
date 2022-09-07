package com.tachyonmusic.user.data

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.user.di.AppModule
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.assertEquals
import com.tachyonmusic.util.tryInject
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class)
class LocalCacheTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Repository is created, localCache is set
    @Inject
    lateinit var repository: UserRepository

    @Inject
    lateinit var localCache: LocalCache

    @Inject
    lateinit var loops: MutableList<Loop>

    @Inject
    lateinit var playlists: MutableList<Playlist>

    @Before
    fun setUp() {
        hiltRule.tryInject()
    }

    @After
    fun cleanUp() {
        localCache.reset()
    }

    @Test
    fun unregisteredUserCacheExists() {
        assert(localCache.exists)
    }

    @Test
    fun newRegisteredUserCacheDeleted() {
        assert(localCache.exists)
        localCache.onUserChanged("SomeUID")
        assert(!localCache.exists)
    }

    @Test
    fun dataStaysInSync(): Unit = runBlocking {
        assert(localCache.exists)

        for (loop in loops)
            repository += loop
        for (playlist in playlists)
            repository += playlist

        repository.upload()

        assert(localCache.exists)

        val metadata = localCache.get()
        val loadedLoops = metadata.loops.await()
        val loadedPlaylists = metadata.playlists.await()

        assertEquals(loadedLoops, loops)
        assertEquals(loadedPlaylists, playlists)
    }

}