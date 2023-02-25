package com.tachyonmusic.domain.repository

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.tachyonmusic.media.core.RepeatMode
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.media.core.SortOrder
import com.tachyonmusic.media.core.SortParameters
import com.tachyonmusic.media.core.SortType
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

    var playback: SinglePlayback?
    var playWhenReady: Boolean

    val playbackState: StateFlow<SinglePlayback?>
    val associatedPlaylistState: StateFlow<Playlist?>
    val playWhenReadyState: StateFlow<Boolean>
    val sortParamsState: StateFlow<SortParameters>

    var sortParams: SortParameters

    /**
     * Start playing specified playlist
     */
    fun playPlaylist(playlist: Playlist?)

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

    val timingDataState: StateFlow<TimingDataController?>

    fun prepare()
    fun stop()
    fun play()
    fun pause()
    fun seekTo(pos: Duration?)
    fun seekForward()
    fun seekBack()

    /**
     * Find [playback] in current [associatedPlaylistState] and start it
     */
    fun seekTo(playback: SinglePlayback, pos: Duration? = null)

    interface EventListener {
        fun onConnected() {}
    }

}