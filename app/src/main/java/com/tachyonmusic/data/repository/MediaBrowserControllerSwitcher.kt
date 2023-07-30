package com.tachyonmusic.data.repository

import androidx.lifecycle.Lifecycle
import androidx.media3.common.PlaybackParameters
import com.tachyonmusic.TachyonApplication
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.data.playback.SpotifyPlaylist
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.media.domain.use_case.SaveRecentlyPlayed
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class MediaBrowserControllerSwitcher(
    private val localBrowser: MediaPlaybackServiceMediaBrowserController,
    private val spotifyBrowser: SpotifyMediaBrowserController,
    application: TachyonApplication,
    saveRecentlyPlayed: SaveRecentlyPlayed,
    private val addNewPlaybackToHistory: AddNewPlaybackToHistory
) : MediaBrowserController, MediaBrowserController.EventListener,
    IListenable<MediaBrowserController.EventListener> by Listenable() {

    private var playbackLocation = MutableStateFlow(MediaBrowserController.PlaybackLocation.Local)
    private val ioScope = application.coroutineScope + Dispatchers.IO

    init {
        registerEventListener(this)

        combine(playbackLocation, spotifyBrowser.isPlaying) { playbackLocation, isPlaying ->
            if (playbackLocation == MediaBrowserController.PlaybackLocation.Spotify && !isPlaying) {
                val playback = currentPlayback.value ?: return@combine
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPosition ?: return@combine,
                        playback.duration,
                        ArtworkType.REMOTE,
                        (playback.artwork as RemoteArtwork).uri.toString()
                    )
                )
            }
        }.launchIn(ioScope)
    }

    override fun registerLifecycle(lifecycle: Lifecycle) {
        localBrowser.registerLifecycle(lifecycle)
    }

    override fun registerEventListener(listener: MediaBrowserController.EventListener) {
        localBrowser.registerEventListener(listener)
        spotifyBrowser.registerEventListener(listener)
    }

    override fun unregisterEventListener(listener: MediaBrowserController.EventListener) {
        localBrowser.unregisterEventListener(listener)
        spotifyBrowser.unregisterEventListener(listener)
    }

    override val currentPlaylist: StateFlow<Playlist?> = combine(
        localBrowser.currentPlaylist,
        spotifyBrowser.currentPlaylist,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            MediaBrowserController.PlaybackLocation.Local -> local
            MediaBrowserController.PlaybackLocation.Spotify -> spotify ?: local
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, null)

    override val currentPlayback: StateFlow<SinglePlayback?> = combine(
        localBrowser.currentPlayback,
        spotifyBrowser.currentPlayback,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            MediaBrowserController.PlaybackLocation.Local -> local
            MediaBrowserController.PlaybackLocation.Spotify -> spotify
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, null)

    override val isPlaying: StateFlow<Boolean> = combine(
        localBrowser.isPlaying,
        spotifyBrowser.isPlaying,
        playbackLocation
    ) { local, spotify, location ->
        when (location) {
            MediaBrowserController.PlaybackLocation.Local -> local
            MediaBrowserController.PlaybackLocation.Spotify -> spotify
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, false)


    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        when (playlist) {
            is LocalPlaylist -> {
                when (playlist.current?.underlyingSong) {
                    is LocalSong -> {
                        localBrowser.setPlaylist(playlist, position)
                        playbackLocation.update { MediaBrowserController.PlaybackLocation.Local }
                    }

                    is SpotifySong -> {
                        spotifyBrowser.play(playlist.current ?: return)
                        spotifyBrowser.seekTo(position)
                        playbackLocation.update { MediaBrowserController.PlaybackLocation.Spotify }
                    }

                    else -> error("Invalid song type ${playlist.current?.underlyingSong}")
                }

            }

            is SpotifyPlaylist -> {
                spotifyBrowser.setPlaylist(playlist, position)
                playbackLocation.update { MediaBrowserController.PlaybackLocation.Spotify }
            }

            else -> TODO()
        }
    }

    override val currentPosition: Duration?
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.currentPosition
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.currentPosition
        }

    override var currentPlaybackTimingData: TimingDataController?
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.currentPlaybackTimingData
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.currentPlaybackTimingData
        }
        set(value) {
            when (playbackLocation.value) {
                MediaBrowserController.PlaybackLocation.Local -> localBrowser.currentPlaybackTimingData =
                    value

                MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.currentPlaybackTimingData =
                    value
            }
        }

    override val canPrepare: Boolean
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.canPrepare
            MediaBrowserController.PlaybackLocation.Spotify -> false
        }

    override var playbackParameters: PlaybackParameters
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.playbackParameters
            MediaBrowserController.PlaybackLocation.Spotify -> PlaybackParameters.DEFAULT
        }
        set(value) {
            if (playbackLocation.value == MediaBrowserController.PlaybackLocation.Local)
                localBrowser.playbackParameters = value
        }

    override var volume: Float
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.volume
            MediaBrowserController.PlaybackLocation.Spotify -> 1f
        }
        set(value) {
            if (playbackLocation.value == MediaBrowserController.PlaybackLocation.Local)
                localBrowser.volume = value
        }

    override val audioSessionId: Int?
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.audioSessionId
            MediaBrowserController.PlaybackLocation.Spotify -> 0
        }

    override val nextPlayback: SinglePlayback?
        get() = when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.nextPlayback
            MediaBrowserController.PlaybackLocation.Spotify ->
                if (currentPlaylist.value?.mediaId?.isSpotifyPlaylist == true)
                    spotifyBrowser.nextPlayback
                else
                    localBrowser.nextPlayback
        }

    override val repeatMode: StateFlow<RepeatMode>
        get() = localBrowser.repeatMode // RepeatModes are synchronized and always the same in both browsers

    override fun setRepeatMode(repeatMode: RepeatMode) {
        localBrowser.setRepeatMode(repeatMode)
        spotifyBrowser.setRepeatMode(repeatMode)
    }

    override suspend fun prepare() {
        if (playbackLocation.value == MediaBrowserController.PlaybackLocation.Local)
            localBrowser.prepare()
    }

    override fun play() {
        when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.play()
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.play()
        }
    }

    override fun pause() {
        when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.pause()
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.pause()
        }
    }

    override fun stop() {
        if (playbackLocation.value == MediaBrowserController.PlaybackLocation.Local)
            localBrowser.stop()
    }

    override fun seekTo(pos: Duration?) {
        when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.seekTo(pos)
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.seekTo(pos)
        }
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.seekTo(mediaId, pos)
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.seekTo(mediaId, pos)
        }
    }

    override fun seekTo(index: Int, pos: Duration?) {
        when (playbackLocation.value) {
            MediaBrowserController.PlaybackLocation.Local -> localBrowser.seekTo(index, pos)
            MediaBrowserController.PlaybackLocation.Spotify -> spotifyBrowser.seekTo(index, pos)
        }
    }

    override fun seekToNext() {
        // TODO: Spotify
        localBrowser.seekToNext()
    }

    override fun seekToPrevious() {
        // TODO: Spotify
        localBrowser.seekToPrevious()
    }

    override fun onControlDispatched(playbackLocation: MediaBrowserController.PlaybackLocation) {
        if (this.playbackLocation.value == playbackLocation)
            return

        this.playbackLocation.update { playbackLocation }
//        switchToCorrectPlayer(
//            if (currentPlaylist.value?.playbacks?.contains(playback) == true)
//                playback ?: return
//            else
//                nextPlayback ?: return
//        )
    }

    override fun onMediaItemTransition(
        playback: SinglePlayback?,
        source: MediaBrowserController.PlaybackLocation
    ) {
        if (source == MediaBrowserController.PlaybackLocation.Spotify &&
            playbackLocation.value == MediaBrowserController.PlaybackLocation.Spotify &&
            playback != null
        ) {
            ioScope.launch {
                addNewPlaybackToHistory(playback)
            }
        }
    }


    private fun switchToCorrectPlayer(playback: SinglePlayback) {
        if (playbackLocation.value != MediaBrowserController.PlaybackLocation.Spotify && playback is SpotifySong) {
            localBrowser.pause()
            spotifyBrowser.play(playback)
            playbackLocation.update { MediaBrowserController.PlaybackLocation.Spotify }
        } else if (playbackLocation.value != MediaBrowserController.PlaybackLocation.Local && playback !is SpotifySong) {
            spotifyBrowser.pause()
            localBrowser.seekTo(playback.mediaId)
            localBrowser.play()
            playbackLocation.update { MediaBrowserController.PlaybackLocation.Local }
        }
    }
}