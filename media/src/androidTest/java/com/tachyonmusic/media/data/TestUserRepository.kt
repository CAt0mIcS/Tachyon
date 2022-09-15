package com.tachyonmusic.media.data

import com.tachyonmusic.util.Resource
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.launch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class TestUserRepository : UserRepository {
    override val songs: CompletableDeferred<List<Song>> = CompletableDeferred()
    override val loops: CompletableDeferred<List<Loop>> = CompletableDeferred()
    override val playlists: CompletableDeferred<List<Playlist>> = CompletableDeferred()

    override val signedIn: Boolean
        get() = TODO("Not yet implemented")

    fun complete(
        songs: List<Song>,
        loops: List<Loop>,
        playlists: List<Playlist>,
        delay: Long? = null
    ) {
        launch(Dispatchers.Main) {
            if (delay != null)
                delay(delay)

            this@TestUserRepository.songs.complete(songs)
            this@TestUserRepository.loops.complete(loops)
            this@TestUserRepository.playlists.complete(playlists)
        }

    }

    override suspend fun signIn(email: String, password: String): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun register(email: String, password: String): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override fun signOut() {
        TODO("Not yet implemented")
    }

    override fun upload() {
        TODO("Not yet implemented")
    }

    override fun registerEventListener(listener: UserRepository.EventListener?) {
        TODO("Not yet implemented")
    }
}