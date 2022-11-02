package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.ListenableMutableList
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.util.IListenable
import kotlinx.coroutines.flow.Flow

interface MediaBrowserController : IListenable<MediaBrowserController.EventListener>,
    DefaultLifecycleObserver {

    /**
     * Binds a lifecycle object to the [MediaBrowserController]
     */
    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    var playback: Playback?

    var playWhenReady: Boolean

    fun getChildren(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>

    fun getPlaybacksNative(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): List<Playback>

    // TODO: Shouldn't use liveData in repositories
    val songs: LiveData<List<Song>>
    val loops: LiveData<List<Loop>>
    val playlists: LiveData<List<Playlist>>

    operator fun plusAssign(song: Song)

    val isPlaying: Boolean

    val title: String?
    val artist: String?
    val name: String?
    val duration: Long?
    var timingData: MutableList<TimingData>?
    val currentPosition: Long?

    fun stop()
    fun play()
    fun pause()
    fun seekTo(pos: Long)

    interface EventListener {
        fun onConnected() {}
        fun onPlaybackTransition(playback: Playback?) {}
    }
}