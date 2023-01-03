package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.util.IListenable

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

    suspend fun getChildren(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): LibraryResult<ImmutableList<MediaItem>>

    suspend fun getPlaybacksNative(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): List<Playback>

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
    fun seekForward()
    fun seekBack()

    interface EventListener {
        fun onConnected() {}
        fun onPlaybackTransition(playback: Playback?) {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
    }
}