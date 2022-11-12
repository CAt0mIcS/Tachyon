package com.tachyonmusic.user.domain

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.StateFlow

interface UserRepository : IListenable<UserRepository.EventListener> {
    val songs: StateFlow<List<Song>>
    val loops: StateFlow<List<Loop>>
    val playlists: StateFlow<List<Playlist>>
    val history: StateFlow<List<Playback>>

    val signedIn: Boolean

    suspend fun signIn(
        email: String,
        password: String
    ): Resource<Unit>

    suspend fun register(
        email: String,
        password: String
    ): Resource<Unit>

    fun signOut()

    suspend fun delete(): Resource<Unit>

    fun find(mediaId: MediaId): Playback? {
        val s = songs.value.find { it.mediaId == mediaId }
        if (s != null)
            return s
        val l = loops.value.find { it.mediaId == mediaId }
        if (l != null)
            return l
        return playlists.value.find { it.mediaId == mediaId }
    }

    suspend fun save(): Resource<Unit>

    operator fun plusAssign(song: Song)
    operator fun plusAssign(loop: Loop)
    operator fun plusAssign(playlist: Playlist)
    operator fun minusAssign(song: Song)
    operator fun minusAssign(loop: Loop)
    operator fun minusAssign(playlist: Playlist)

    interface EventListener {
        fun onUserChanged(uid: String?) {}
    }
}