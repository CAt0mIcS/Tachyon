package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Duration

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

    var repeatMode: RepeatMode
    val nextMediaItemIndex: Int

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
    val duration: Duration?
    var timingData: TimingDataController?
    val currentPosition: Duration?

    fun stop()
    fun play()
    fun pause()
    fun seekTo(pos: Duration)
    fun seekForward()
    fun seekBack()

    interface EventListener {
        fun onConnected() {}

        /**
         * @param playback the new playback (song or loop) to be played
         * @param associatedPlaylist if we set [MediaBrowserController.playback] to a [Playlist]
         *      then [playback] will be the song/loop currently playing and [associatedPlaylist]
         *      will be the set Playlist
         */
        fun onPlaybackTransition(playback: SinglePlayback?, associatedPlaylist: Playlist?) {}
        fun onIsPlayingChanged(isPlaying: Boolean) {}
        fun onTimingDataAdvanced(i: Int) {}
    }
}