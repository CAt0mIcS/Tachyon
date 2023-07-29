package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.media.util.fromMedia
import com.tachyonmusic.media.util.playback
import com.tachyonmusic.media.util.toMediaItems
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.predefinedCustomizedSongPlaylistMediaId
import com.tachyonmusic.playback_layers.predefinedSongPlaylistMediaId
import com.tachyonmusic.util.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MediaPlaybackServiceMediaBrowserController(
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val predefinedPlaylistsRepository: PredefinedPlaylistsRepository,
    private val log: Logger
) : MediaBrowserController, Player.Listener,
    MediaBrowser.Listener, IListenable<MediaBrowserController.EventListener> by Listenable() {

    private var browser: MediaBrowser? = null

    override fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        // TODO: Does this need to be done in onStart/onResume?
        // TODO: Should we disconnect in onStop?

        if (owner !is Activity)
            TODO("MediaBrowserController must be created in an activity")

        val sessionToken = SessionToken(
            owner,
            ComponentName(owner, MediaPlaybackService::class.java)
        )

        owner.lifecycleScope.launch {
            browser = MediaBrowser.Builder(owner, sessionToken)
                .setListener(this@MediaPlaybackServiceMediaBrowserController)
                .buildAsync().await()
            browser?.addListener(this@MediaPlaybackServiceMediaBrowserController)
            invokeEvent {
                it.onConnected()
            }
        }

        /**
         * Make sure that if the predefined playlists change the playlist in the player gets
         * updated, too. For example when changing the combine songs and customized songs in playlist
         * setting
         */
        predefinedPlaylistsRepository.songPlaylist.onEach {
            if (!canPrepare && currentPlaylist.value?.mediaId == predefinedSongPlaylistMediaId) {
                log.info("Updating player with new predefined song playlist during playback")
                val prevPosition = currentPosition
                val prevPb = currentPlayback.value ?: return@onEach
                setPlaylist(getPlaylistForPlayback(prevPb) ?: return@onEach)
                seekTo(prevPb.mediaId, prevPosition)
            }
        }.launchIn(owner.lifecycleScope)

        predefinedPlaylistsRepository.customizedSongPlaylist.onEach {
            if (!canPrepare && currentPlaylist.value?.mediaId == predefinedCustomizedSongPlaylistMediaId) {
                log.info("Updating player with new predefined customized song playlist during playback")
                val prevPosition = currentPosition
                val prevPb = currentPlayback.value ?: return@onEach
                setPlaylist(getPlaylistForPlayback(prevPb) ?: return@onEach)
                seekTo(prevPb.mediaId, prevPosition)
            }
        }.launchIn(owner.lifecycleScope)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        browser?.release()
    }


    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    private val _currentPlayback = MutableStateFlow<SinglePlayback?>(null)
    override val currentPlayback: StateFlow<SinglePlayback?> = _currentPlayback.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        browser?.setMediaItems(
            playlist.playbacks.toMediaItems(),
            playlist.currentPlaylistIndex,
            position?.inWholeMilliseconds ?: 0
        )
        _currentPlaylist.update { playlist }
        _currentPlayback.update { playlist.current }
    }

    override val currentPosition: Duration?
        get() = if (browser?.currentTimeline?.isEmpty == true || browser?.isConnected != true)
            null
        else browser?.currentPosition?.ms

    override var currentPlaybackTimingData: TimingDataController?
        get() = currentPlayback.value?.timingData
        set(value) {
            if (value != null)
                browser?.dispatchMediaEvent(SetTimingDataEvent(value))
        }

    override val canPrepare: Boolean
        get() = browser?.isConnected == true
                && browser?.playbackState == Player.STATE_IDLE
                && (browser?.mediaItemCount ?: -1) > 0
                && currentPlaylist.value != null

    override var playbackParameters: PlaybackParameters
        get() = browser?.playbackParameters ?: PlaybackParameters.DEFAULT
        set(value) {
            browser?.playbackParameters = value
        }

    override var volume: Float
        get() = browser?.volume ?: 1f
        set(value) {
            browser?.volume = value
        }

    override var audioSessionId: Int? = null
        private set

    override val nextPlayback: SinglePlayback?
        get() {
            val idx = browser?.nextMediaItemIndex
            if (idx == null || idx >= browser!!.mediaItemCount || idx < 0)
                return null
            return browser?.getMediaItemAt(idx)?.mediaMetadata?.playback
        }

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.All)
    override val repeatMode = _repeatMode.asStateFlow()

    override fun setRepeatMode(repeatMode: RepeatMode) {
        browser?.dispatchMediaEvent(SetRepeatModeEvent(repeatMode))
    }

    private var prepareJob: CompletableJob? = null
    override suspend fun prepare() {
        assert(currentPlaylist.value != null)

        prepareJob = Job()
        browser?.prepare()
        prepareJob?.join()
    }

    override fun play() {
        browser?.play()
    }

    override fun pause() {
        browser?.pause()
    }

    override fun stop() {
        browser?.stop()
        browser?.clearMediaItems()

        _currentPlayback.update { null }
        _currentPlaylist.update { null }
        _isPlaying.update { false }
    }

    override fun seekTo(pos: Duration?) {
        browser?.seekTo(pos?.inWholeMilliseconds ?: C.TIME_UNSET)
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        seekTo(browser?.indexOf(mediaId) ?: return, pos)
    }

    override fun seekTo(index: Int, pos: Duration?) {
        browser?.seekTo(index, pos?.inWholeMilliseconds ?: C.TIME_UNSET)

        /**
         * If we seek to the first item in the playlist [onMediaItemTransition] will only be called
         * with [Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED] and [currentPlayback] won't be updated
         */
        if (index == 0) {
            _currentPlayback.update { browser?.getMediaItemAt(0)?.mediaMetadata?.playback }
        }
    }

    override fun seekToNext() {
        browser?.seekToNext()
    }

    override fun seekToPrevious() {
        browser?.seekToPrevious()
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
            _currentPlayback.update { mediaItem?.mediaMetadata?.playback }
        invokeEvent { it.onMediaItemTransition(mediaItem?.mediaMetadata?.playback) }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
            prepareJob?.complete()
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        _isPlaying.update { playWhenReady }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        if (repeatMode == 0)
            return // TODO: Handle repeat mode for cast player

        _repeatMode.update {
            RepeatMode.fromMedia(
                repeatMode,
                browser?.shuffleModeEnabled ?: false
            )
        }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        _repeatMode.update {
            RepeatMode.fromMedia(
                browser?.repeatMode ?: Player.REPEAT_MODE_ALL,
                shuffleModeEnabled
            )
        }
    }

    override fun onDisconnected(controller: MediaController) {
        _currentPlayback.update { null }
        _currentPlaylist.update { null }
    }

    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> = future(Dispatchers.IO) {
        when (val event = command.toMediaSessionEvent(args)) {
            is TimingDataUpdatedEvent -> {
                log.info("Received timing data updated event with ${event.timingData} for playback: ${currentPlayback.value}")
                _currentPlayback.update {
                    it?.copy()?.apply {
                        timingData = event.timingData
                    }
                }
            }

            is StateUpdateEvent -> {
                log.info("Received state update event with ${event.currentPlayback}, playWhenReady=${event.playWhenReady}")
                _currentPlayback.update { event.currentPlayback }
                _isPlaying.update { event.playWhenReady }
                _currentPlaylist.update { event.currentPlaylist }
                _repeatMode.update { event.repeatMode }
            }

            is AudioSessionIdChangedEvent -> {
                log.info("Received new audio session id ${event.audioSessionId}")
                onAudioSessionIdChanged(event.audioSessionId)
            }

        }

        SessionResult(SessionResult.RESULT_SUCCESS)
    }

    // TODO: Not working, currently using custom command
    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        this.audioSessionId = audioSessionId
        invokeEvent { it.onAudioSessionIdChanged(audioSessionId) }
    }
}


private fun MediaBrowser.indexOf(mediaId: MediaId): Int? {
    for (i in 0 until mediaItemCount) {
        if (getMediaItemAt(i).mediaId == mediaId.toString())
            return i
    }
    return null
}