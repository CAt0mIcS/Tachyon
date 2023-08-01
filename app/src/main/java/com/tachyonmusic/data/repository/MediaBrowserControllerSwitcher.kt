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
import com.tachyonmusic.media.domain.model.MediaSyncEventListener
import com.tachyonmusic.media.domain.model.PlaybackController
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
) : MediaBrowserController, MediaSyncEventListener,
    IListenable<MediaSyncEventListener> by Listenable() {

    private var playbackController = MutableStateFlow(PlaybackController.Local)
    private val ioScope = application.coroutineScope + Dispatchers.IO

    init {
        registerEventListener(this)

        combine(playbackController, spotifyBrowser.isPlaying) { playbackController, isPlaying ->
            if (playbackController == PlaybackController.Spotify && !isPlaying) {
                val playback = spotifyBrowser.currentPlayback.value ?: return@combine
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPosition ?: return@combine,
                        playback.duration,
                        ArtworkType.getType(playback),
                        if (playback.artwork is RemoteArtwork)
                            (playback.artwork as RemoteArtwork).uri.toURL().toString()
                        else null
                    )
                )
            }
        }.launchIn(ioScope)
    }

    override fun registerLifecycle(lifecycle: Lifecycle) {
        localBrowser.registerLifecycle(lifecycle)
    }

    override fun registerEventListener(listener: MediaSyncEventListener) {
        localBrowser.registerEventListener(listener)
        spotifyBrowser.registerEventListener(listener)
    }

    override fun unregisterEventListener(listener: MediaSyncEventListener) {
        localBrowser.unregisterEventListener(listener)
        spotifyBrowser.unregisterEventListener(listener)
    }

    override val currentPlaylist: StateFlow<Playlist?> = combine(
        localBrowser.currentPlaylist,
        spotifyBrowser.currentPlaylist,
        playbackController
    ) { local, spotify, location ->
        when (location) {
            PlaybackController.Local -> local
            PlaybackController.Spotify -> spotify ?: local
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, null)

    override val currentPlayback: StateFlow<SinglePlayback?> = combine(
        localBrowser.currentPlayback,
        spotifyBrowser.currentPlayback,
        playbackController
    ) { local, spotify, location ->
        when (location) {
            PlaybackController.Local -> local
            PlaybackController.Spotify -> spotify
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, null)

    override val isPlaying: StateFlow<Boolean> = combine(
        localBrowser.isPlaying,
        spotifyBrowser.isPlaying,
        playbackController
    ) { local, spotify, location ->
        when (location) {
            PlaybackController.Local -> local
            PlaybackController.Spotify -> spotify
        }
    }.stateIn(ioScope, SharingStarted.Eagerly, false)


    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        when (playlist) {
            is LocalPlaylist -> {
                when (playlist.current?.underlyingSong) {
                    is LocalSong -> {
                        localBrowser.setPlaylist(playlist, position)
                        playbackController.update { PlaybackController.Local }
                    }

                    is SpotifySong -> {
                        spotifyBrowser.play(playlist.current ?: return)
                        spotifyBrowser.seekTo(position)
                        playbackController.update { PlaybackController.Spotify }
                    }

                    else -> error("Invalid song type ${playlist.current?.underlyingSong}")
                }

            }

            is SpotifyPlaylist -> {
                spotifyBrowser.setPlaylist(playlist, position)
                playbackController.update { PlaybackController.Spotify }
            }

            else -> TODO()
        }
    }

    override val currentPosition: Duration?
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.currentPosition
            PlaybackController.Spotify -> spotifyBrowser.currentPosition
        }

    override var currentPlaybackTimingData: TimingDataController?
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.currentPlaybackTimingData
            PlaybackController.Spotify -> spotifyBrowser.currentPlaybackTimingData
        }
        set(value) {
            when (playbackController.value) {
                PlaybackController.Local -> localBrowser.currentPlaybackTimingData =
                    value

                PlaybackController.Spotify -> spotifyBrowser.currentPlaybackTimingData =
                    value
            }
        }

    override val canPrepare: Boolean
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.canPrepare
            PlaybackController.Spotify -> false
        }

    override var playbackParameters: PlaybackParameters
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.playbackParameters
            PlaybackController.Spotify -> PlaybackParameters.DEFAULT
        }
        set(value) {
            if (playbackController.value == PlaybackController.Local)
                localBrowser.playbackParameters = value
        }

    override var volume: Float
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.volume
            PlaybackController.Spotify -> 1f
        }
        set(value) {
            if (playbackController.value == PlaybackController.Local)
                localBrowser.volume = value
        }

    override val audioSessionId: Int?
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.audioSessionId
            PlaybackController.Spotify -> 0
        }

    override val nextPlayback: SinglePlayback?
        get() = when (playbackController.value) {
            PlaybackController.Local -> localBrowser.nextPlayback
            PlaybackController.Spotify ->
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
        if (playbackController.value == PlaybackController.Local)
            localBrowser.prepare()
    }

    override fun play() {
        when (playbackController.value) {
            PlaybackController.Local -> localBrowser.play()
            PlaybackController.Spotify -> spotifyBrowser.play()
        }
    }

    override fun pause() {
        when (playbackController.value) {
            PlaybackController.Local -> localBrowser.pause()
            PlaybackController.Spotify -> spotifyBrowser.pause()
        }
    }

    override fun stop() {
        if (playbackController.value == PlaybackController.Local)
            localBrowser.stop()
    }

    override fun seekTo(pos: Duration?) {
        when (playbackController.value) {
            PlaybackController.Local -> localBrowser.seekTo(pos)
            PlaybackController.Spotify -> spotifyBrowser.seekTo(pos)
        }
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        when (playbackController.value) {
            PlaybackController.Local -> localBrowser.seekTo(mediaId, pos)
            PlaybackController.Spotify -> spotifyBrowser.seekTo(mediaId, pos)
        }
    }

    override fun seekTo(index: Int, pos: Duration?) {
        when (playbackController.value) {
            PlaybackController.Local -> localBrowser.seekTo(index, pos)
            PlaybackController.Spotify -> spotifyBrowser.seekTo(index, pos)
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

    override fun onControlDispatched(playbackController: PlaybackController) {
        if (this.playbackController.value == playbackController)
            return

        this.playbackController.update { playbackController }
//        switchToCorrectPlayer(
//            if (currentPlaylist.value?.playbacks?.contains(playback) == true)
//                playback ?: return
//            else
//                nextPlayback ?: return
//        )
    }

    override fun onMediaItemTransition(
        playback: SinglePlayback?,
        source: PlaybackController
    ) {
        if (source == PlaybackController.Spotify &&
            playbackController.value == PlaybackController.Spotify &&
            playback != null
        ) {
            ioScope.launch {
                addNewPlaybackToHistory(playback)
            }
        }
    }


    private fun switchToCorrectPlayer(playback: SinglePlayback) {
        if (playbackController.value != PlaybackController.Spotify && playback is SpotifySong) {
            localBrowser.pause()
            spotifyBrowser.play(playback)
            playbackController.update { PlaybackController.Spotify }
        } else if (playbackController.value != PlaybackController.Local && playback !is SpotifySong) {
            spotifyBrowser.pause()
            localBrowser.seekTo(playback.mediaId)
            localBrowser.play()
            playbackController.update { PlaybackController.Local }
        }
    }
}