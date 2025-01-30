package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.core.AudioSessionIdChangedEvent
import com.tachyonmusic.media.core.PlaybackUpdateEvent
import com.tachyonmusic.media.core.SessionSyncEvent
import com.tachyonmusic.media.core.SetRepeatModeEvent
import com.tachyonmusic.media.core.dispatchMediaEvent
import com.tachyonmusic.media.core.toMediaSessionEvent
import com.tachyonmusic.media.util.fromMedia
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.isPredefined
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.future
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MediaPlaybackServiceMediaBrowserController(
    private val getPlaylistForPlayback: GetPlaylistForPlayback,
    private val log: Logger,
    private val playbackRepository: PlaybackRepository
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
            ComponentName(owner, AppMediaPlaybackService::class.java)
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
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        browser?.release()
    }


    private val _currentPlaylist = MutableStateFlow<Playlist?>(null)
    override val currentPlaylist: StateFlow<Playlist?> = _currentPlaylist.asStateFlow()

    private val _currentPlayback = MutableStateFlow<Playback?>(null)
    override val currentPlayback: StateFlow<Playback?> = _currentPlayback.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    override fun setPlaylist(playlist: Playlist, position: Duration?) {
        browser?.setMediaItems(
            playlist.playbacks.map { it.toMediaItem() },
            playlist.currentPlaylistIndex,
            position?.inWholeMilliseconds ?: 0
        )
        _currentPlaylist.update { playlist }
        updatePlayback { playlist.current }
    }

    override fun updatePlayback(action: (Playback?) -> Playback?) {
        val newPlayback = action(currentPlayback.value)
        _currentPlayback.update { newPlayback }
        browser?.dispatchMediaEvent(PlaybackUpdateEvent(newPlayback, currentPlaylist.value))
    }

    override val currentPosition: Duration?
        get() = if (browser?.currentTimeline?.isEmpty == true || browser?.isConnected != true)
            null
        else browser?.currentPosition?.ms

    override val canPrepare: Boolean
        get() = browser?.isConnected == true
                && browser?.playbackState == Player.STATE_IDLE
                && (browser?.mediaItemCount ?: -1) > 0
                && currentPlaylist.value != null

    override var audioSessionId: Int? = null
        private set

    override val nextPlayback: Playback?
        get() {
            val idx = browser?.nextMediaItemIndex
            if (idx == null || idx >= browser!!.mediaItemCount || idx < 0)
                return null
            return browser?.getMediaItemAt(idx)?.let { Playback.fromMediaItem(it) }
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
            val playback = browser?.getMediaItemAt(0)?.let { Playback.fromMediaItem(it) }
            updatePlayback { playback }
        }
    }

    override fun seekToNext() {
        browser?.seekToNext()
    }

    override fun seekToPrevious() {
        browser?.seekToPrevious()
    }

    override suspend fun updatePredefinedPlaylist() =
        withContext(Dispatchers.Main) {
            if (!canPrepare && currentPlaylist.value?.mediaId?.isPredefined == true) {
                log.info("Updating player with new predefined song or remix playlist during playback")
                val prevPosition = currentPosition
                val prevPb = currentPlayback.value ?: return@withContext
                val playlist = withContext(Dispatchers.IO) { getPlaylistForPlayback(prevPb) }
                    ?: return@withContext
                setPlaylist(playlist)
                seekTo(prevPb.mediaId, prevPosition)
            }
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
            is PlaybackUpdateEvent -> {
                log.info("Received state update event with ${event.currentPlayback}")
                _currentPlayback.update { event.currentPlayback }
                _currentPlaylist.update { event.currentPlaylist }
            }

            is SessionSyncEvent -> {
                log.info("Received state update event with playWhenReady=${event.playWhenReady}")
                val latest = playbackRepository.historyFlow.first().find { it.isPlayable }
                val newPlayback =
                    if (event.currentPlayback != null && event.currentPlayback?.mediaId == latest?.mediaId)
                        event.currentPlayback
                    else
                        latest
                _currentPlaylist.update { event.currentPlaylist }
                _isPlaying.update { event.playWhenReady }

                runOnUiThread { updatePlayback { newPlayback } }
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