package com.tachyonmusic.data.repository

import androidx.media3.common.PlaybackParameters
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import kotlinx.coroutines.flow.StateFlow

class MediaBrowserControllerSwitcher : MediaBrowserController,
    IListenable<MediaBrowserController.EventListener> by Listenable() {
    override val currentPlaylist: StateFlow<Playlist?>
        get() = TODO("Not yet implemented")
    override val currentPlayback: StateFlow<SinglePlayback?>
        get() = TODO("Not yet implemented")
    override val isPlaying: StateFlow<Boolean>
        get() = TODO("Not yet implemented")

    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        TODO("Not yet implemented")
    }

    override val currentPosition: Duration?
        get() = TODO("Not yet implemented")
    override var currentPlaybackTimingData: TimingDataController?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val canPrepare: Boolean
        get() = TODO("Not yet implemented")
    override var playbackParameters: PlaybackParameters
        get() = TODO("Not yet implemented")
        set(value) {}
    override var volume: Float
        get() = TODO("Not yet implemented")
        set(value) {}
    override val audioSessionId: Int?
        get() = TODO("Not yet implemented")
    override val nextPlayback: SinglePlayback?
        get() = TODO("Not yet implemented")
    override val repeatMode: StateFlow<RepeatMode>
        get() = TODO("Not yet implemented")

    override fun setRepeatMode(repeatMode: RepeatMode) {
        TODO("Not yet implemented")
    }

    override suspend fun prepare() {
        TODO("Not yet implemented")
    }

    override fun play() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun seekTo(pos: Duration?) {
        TODO("Not yet implemented")
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        TODO("Not yet implemented")
    }

    override fun seekTo(index: Int, pos: Duration?) {
        TODO("Not yet implemented")
    }
}