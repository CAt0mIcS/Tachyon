package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import kotlinx.coroutines.flow.StateFlow


interface MediaBrowserController : DefaultLifecycleObserver,
    IListenable<MediaBrowserController.EventListener> {

    /**
     * Binds a lifecycle object to the [MediaBrowserController]
     */
    fun registerLifecycle(lifecycle: Lifecycle)

    val currentPlaylist: StateFlow<Playlist?>
    val currentPlayback: StateFlow<Playback?>
    val isPlaying: StateFlow<Boolean>

    /**
     * Set and load a new playlist
     */
    fun setPlaylist(playlist: Playlist, position: Duration? = null)

    /**
     * Update the currently playing playback (given as argument in [action])
     * with new information (like new timing data, playback parameters, audio effects, ...)
     */
    fun updatePlayback(action: (Playback?) -> Playback?)

    val currentPosition: Duration?
    val canPrepare: Boolean
    val audioSessionId: Int?

    val nextPlayback: Playback?
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

    fun seekToNext()
    fun seekToPrevious()

    suspend fun updatePredefinedPlaylist()

    interface EventListener {
        fun onConnected() {}
        fun onAudioSessionIdChanged(audioSessionId: Int) {}
    }
}