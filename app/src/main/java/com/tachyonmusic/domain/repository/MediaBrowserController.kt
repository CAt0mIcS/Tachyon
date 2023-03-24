package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import kotlinx.coroutines.flow.StateFlow


interface MediaBrowserController : DefaultLifecycleObserver,
    IListenable<MediaBrowserController.EventListener> {

    /**
     * Binds a lifecycle object to the [MediaBrowserController]
     */
    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    val currentPlaylist: StateFlow<Playlist?>
    val currentPlayback: StateFlow<SinglePlayback?>
    val isPlaying: StateFlow<Boolean>


    fun setPlaylist(playlist: Playlist)

    val currentPosition: Duration?
    var currentPlaybackTimingData: TimingDataController?
    val canPrepare: Boolean

    val nextPlayback: SinglePlayback?

    val repeatMode: StateFlow<RepeatMode>

    fun setRepeatMode(repeatMode: RepeatMode)

    suspend fun prepare()
    fun play()
    fun pause()
    fun stop()

    /**
     * Seeks to [pos] in the current media item
     */
    fun seekTo(pos: Duration?)

    /**
     * Seeks to the playback with the [mediaId] in [currentPlaylist]
     */
    fun seekTo(mediaId: MediaId, pos: Duration? = null)

    /**
     * Seeks to the [index] in the [currentPlaylist]
     */
    fun seekTo(index: Int, pos: Duration? = null)

    interface EventListener {
        fun onConnected() {}
    }
}