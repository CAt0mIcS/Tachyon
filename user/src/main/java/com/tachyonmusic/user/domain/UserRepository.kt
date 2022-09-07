package com.tachyonmusic.user.domain

import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.IListenable
import kotlinx.coroutines.Deferred

interface UserRepository : IListenable<UserRepository.EventListener> {
    val songs: Deferred<List<Song>>
    val loops: Deferred<List<Loop>>
    val playlists: Deferred<List<Playlist>>

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

    suspend fun find(mediaId: MediaId): Playback? {
        val s = songs.await().find { it.mediaId == mediaId }
        if (s != null)
            return s
        val l = loops.await().find { it.mediaId == mediaId }
        if (l != null)
            return l
        return playlists.await().find { it.mediaId == mediaId }
    }

    suspend fun upload(): Resource<Unit>

    suspend operator fun plusAssign(song: Song)
    suspend operator fun plusAssign(loop: Loop)
    suspend operator fun plusAssign(playlist: Playlist)

    interface EventListener {
        fun onUserChanged(uid: String?) {}

        fun onSongListChanged(song: Song) {}
        fun onLoopListChanged(loop: Loop) {}
        fun onPlaylistListChanged(playlist: Playlist) {}

        fun onPlaylistChanged(playlist: Playlist) {}
    }
}