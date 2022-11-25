package com.tachyonmusic.user.data.repository

import android.annotation.SuppressLint
import com.tachyonmusic.core.data.playback.AbstractLoop
import com.tachyonmusic.core.data.playback.Playlist
import com.tachyonmusic.testutils.assertEquals
import com.tachyonmusic.testutils.assertResource
import com.tachyonmusic.testutils.tryInject
import com.tachyonmusic.user.data.LocalCache
import com.tachyonmusic.user.di.AppModule
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.*
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
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

    @Inject
    lateinit var localCache: LocalCache

    @Inject
    lateinit var loops: MutableList<AbstractLoop>

    @Inject
    lateinit var playlists: MutableList<Playlist>

    @Before
    fun setUp() {
        cleanUp()
    }

    @After
    fun cleanUp() = runBlocking {
        hiltRule.tryInject()
        if (repository.signedIn || repository.signIn(
                TEST_EMAIL,
                TEST_PASSWORD
            ) is Resource.Success
        ) {
            assertResource(repository.delete())
        }
    }

    /**
     * Completely new user first uploads loops and playlists, while not being registered yet.
     * Then registers new user
     * --> Check that the repository-stored loops and playlists match the ones previously uploaded
     */
    @Test
    fun unregisteredUploadRegisteredDownload(): Unit = runBlocking {
        for (loop in loops)
            repository += loop
        for (playlist in playlists)
            repository += playlist

        assertResource(repository.save())

        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))

        assertEquals(repository.loops.value, loops)
        assertEquals(repository.playlists.value, playlists)
    }

    @Test
    fun cacheResetAfterUserSignedOut(): Unit = runBlocking {
//        for (loop in loops)
//            repository += loop
//        for (playlist in playlists)
//            repository += playlist
//
//        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))
//        assertResource(repository.save())
//
//        repository.signOut()
//
//        assertEquals(repository.loops.value, emptyList())
//        assertEquals(repository.playlists.value, emptyList())
        // TODO: Saving repository is weird right now...
    }

    @Test
    fun cacheResetAfterUserDeleted(): Unit = runBlocking {
//        for (loop in loops)
//            repository += loop
//        for (playlist in playlists)
//            repository += playlist
//
//        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))
//        assertResource(repository.save())
//
//        repository.delete()
//
//        assertEquals(repository.loops.value, emptyList())
//        assertEquals(repository.playlists.value, emptyList())
        // TODO: Saving repository is weird right now...
    }


    @Test
    fun registerUIDFileCreated(): Unit = runBlocking {
        assert(localCache.uidFile?.exists() != true)

        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))

        assert(localCache.uidFile?.exists() == true)
    }

    @Test
    fun signInUIDFileCreated(): Unit = runBlocking {
        assert(localCache.uidFile?.exists() != true)
        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))
        repository.signOut()

        assertResource(repository.signIn(TEST_EMAIL, TEST_PASSWORD))

        assert(localCache.uidFile?.exists() == true)
    }

    @Test
    fun signOutUIDFileDeleted(): Unit = runBlocking {
        assert(localCache.uidFile?.exists() != true)
        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))
        assert(localCache.uidFile?.exists() == true)

        repository.signOut()

        assert(localCache.uidFile?.exists() != true)
    }

    @Test
    fun deleteUserUIDFileDeleted(): Unit = runBlocking {
        assert(localCache.uidFile?.exists() != true)
        assertResource(repository.register(TEST_EMAIL, TEST_PASSWORD))
        assert(localCache.uidFile?.exists() == true)

        repository.delete()

        assert(localCache.uidFile?.exists() != true)
    }
}