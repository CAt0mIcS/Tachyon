package com.tachyonmusic.data.repository

import androidx.lifecycle.Lifecycle
import androidx.media3.common.PlaybackParameters
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.data.playback.SpotifyPlaylist
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.combineStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class MediaBrowserControllerSwitcher(
    private val localBrowser: MediaPlaybackServiceMediaBrowserController,
    private val spotifyBrowser: SpotifyMediaBrowserController
) : MediaBrowserController,
    IListenable<MediaBrowserController.EventListener> by Listenable() {

    private var playbackLocation = MutableStateFlow(PlaybackLocation.Local)

    override fun registerLifecycle(lifecycle: Lifecycle) {
        localBrowser.registerLifecycle(lifecycle)
    }

    override fun registerEventListener(listener: MediaBrowserController.EventListener) {
        localBrowser.registerEventListener(listener)
    }

    override fun unregisterEventListener(listener: MediaBrowserController.EventListener) {
        localBrowser.unregisterEventListener(listener)
    }

    override val currentPlaylist: StateFlow<Playlist?> = combineStates(
        localBrowser.currentPlaylist,
        spotifyBrowser.currentPlaylist,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            PlaybackLocation.Local -> local
            PlaybackLocation.Spotify -> spotify
        }
    }

    override val currentPlayback: StateFlow<SinglePlayback?> = combineStates(
        localBrowser.currentPlayback,
        spotifyBrowser.currentPlayback,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            PlaybackLocation.Local -> local
            PlaybackLocation.Spotify -> spotify
        }
    }

    override val isPlaying: StateFlow<Boolean> = combineStates(
        localBrowser.isPlaying,
        spotifyBrowser.isPlaying,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            PlaybackLocation.Local -> local
            PlaybackLocation.Spotify -> spotify
        }
    }

    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        when (playlist) {
            is LocalPlaylist -> {
                when (playlist.current) {
                    is LocalSong -> {
                        localBrowser.setPlaylist(playlist, position)
                        playbackLocation.update { PlaybackLocation.Local }
                    }
                    is SpotifySong -> {
                        spotifyBrowser.play(playlist.current ?: return)
                        playbackLocation.update { PlaybackLocation.Spotify }
                    }
                }

            }
            is SpotifyPlaylist -> {
                spotifyBrowser.setPlaylist(playlist, position)
                playbackLocation.update { PlaybackLocation.Spotify }
            }
            else -> TODO()
        }
    }

    override val currentPosition: Duration?
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.currentPosition
            PlaybackLocation.Spotify -> spotifyBrowser.currentPosition
        }

    override var currentPlaybackTimingData: TimingDataController?
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.currentPlaybackTimingData
            PlaybackLocation.Spotify -> spotifyBrowser.currentPlaybackTimingData
        }
        set(value) {
            when (playbackLocation.value) {
                PlaybackLocation.Local -> localBrowser.currentPlaybackTimingData = value
                PlaybackLocation.Spotify -> spotifyBrowser.currentPlaybackTimingData = value
            }
        }

    override val canPrepare: Boolean
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.canPrepare
            PlaybackLocation.Spotify -> false
        }

    override var playbackParameters: PlaybackParameters
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.playbackParameters
            PlaybackLocation.Spotify -> PlaybackParameters.DEFAULT
        }
        set(value) {
            if (playbackLocation.value == PlaybackLocation.Local)
                localBrowser.playbackParameters = value
        }

    override var volume: Float
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.volume
            PlaybackLocation.Spotify -> 1f
        }
        set(value) {
            if (playbackLocation.value == PlaybackLocation.Local)
                localBrowser.volume = value
        }

    override val audioSessionId: Int?
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.audioSessionId
            PlaybackLocation.Spotify -> 0
        }

    override val nextPlayback: SinglePlayback?
        get() = when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.nextPlayback
            PlaybackLocation.Spotify -> spotifyBrowser.nextPlayback
        }

    override val repeatMode: StateFlow<RepeatMode>
        get() = localBrowser.repeatMode // RepeatModes are synchronized and always the same in both browsers

    override fun setRepeatMode(repeatMode: RepeatMode) {
        localBrowser.setRepeatMode(repeatMode)
        spotifyBrowser.setRepeatMode(repeatMode)
    }

    override suspend fun prepare() {
        if (playbackLocation.value == PlaybackLocation.Local)
            localBrowser.prepare()
    }

    override fun play() {
        when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.play()
            PlaybackLocation.Spotify -> spotifyBrowser.play()
        }
    }

    override fun pause() {
        when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.pause()
            PlaybackLocation.Spotify -> spotifyBrowser.pause()
        }
    }

    override fun stop() {
        if (playbackLocation.value == PlaybackLocation.Local)
            localBrowser.stop()
    }

    override fun seekTo(pos: Duration?) {
        when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.seekTo(pos)
            PlaybackLocation.Spotify -> spotifyBrowser.seekTo(pos)
        }
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.seekTo(mediaId, pos)
            PlaybackLocation.Spotify -> spotifyBrowser.seekTo(mediaId, pos)
        }
    }

    override fun seekTo(index: Int, pos: Duration?) {
        when (playbackLocation.value) {
            PlaybackLocation.Local -> localBrowser.seekTo(index, pos)
            PlaybackLocation.Spotify -> spotifyBrowser.seekTo(index, pos)
        }
    }

    private enum class PlaybackLocation {
        Local,
        Spotify
    }
}