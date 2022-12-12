package com.tachyonmusic.media.data

import com.tachyonmusic.core.data.playback.AbstractLoop
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TestUserRepository : UserRepository,
    IListenable<UserRepository.EventListener> by Listenable() {
    override val songs: StateFlow<List<Song>>
        get() = _songs
    override val loops: StateFlow<List<Loop>>
        get() = _loops
    override val playlists: StateFlow<List<Playlist>>
        get() = _playlists
    override val history: StateFlow<List<Playback>>
        get() = _history

    private val _songs = MutableStateFlow(listOf<Song>())
    private val _loops = MutableStateFlow(listOf<Loop>())
    private val _playlists = MutableStateFlow(listOf<Playlist>())
    private val _history = MutableStateFlow(listOf<Playback>())


    override val signedIn: Boolean
        get() = TODO("Not yet implemented")

    fun complete(
        songs: List<Song>,
        loops: List<AbstractLoop>,
        playlists: List<Playlist>,
        delay: Long? = null
    ) {
        launch(Dispatchers.Main) {
            if (delay != null)
                delay(delay)

            _songs.value = songs
            _loops.value = loops
            _playlists.value = playlists
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

    override suspend fun delete(): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun save(): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override fun addHistory(playback: Playback) {
        TODO("Not yet implemented")
    }

    override fun plusAssign(song: Song) {
        _songs.value += song
    }

    override fun plusAssign(loop: Loop) {
        _loops.value += loop
    }

    override fun plusAssign(playlist: Playlist) {
        _playlists.value += playlist
    }

    override fun minusAssign(song: Song) {
        _songs.value -= song
    }

    override fun minusAssign(loop: Loop) {
        _loops.value -= loop
    }

    override fun minusAssign(playlist: Playlist) {
        _playlists.value -= playlist
    }
}