package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.media.util.*
import com.tachyonmusic.permission.isPlayable
import com.tachyonmusic.util.*
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MediaPlaybackServiceMediaBrowserController(
    private val context: Context
) : MediaBrowserController, Player.Listener,
    MediaBrowser.Listener, IListenable<MediaBrowserController.EventListener> by Listenable() {

    private var browser: MediaBrowser? = null


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

    override fun setPlaylist(playlist: Playlist) {
        browser?.setMediaItems(playlist.playbacks.toMediaItems())
        _currentPlaylist.update { playlist }
    }

    override val currentPosition: Duration?
        get() = browser?.currentPosition?.ms

    override val canPrepare: Boolean
        get() = browser?.isConnected == true
                && browser?.playbackState == Player.STATE_IDLE
                && (browser?.mediaItemCount ?: -1) > 0

    override val nextPlayback: SinglePlayback?
        get() {
            val idx = browser?.currentMediaItemIndex
            if (idx == null || idx > browser!!.mediaItemCount || idx < 0)
                return null
            return browser?.getMediaItemAt(idx)?.mediaMetadata?.playback
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

    override fun seekTo(pos: Duration?) {
        browser?.seekTo(pos?.inWholeMilliseconds ?: C.TIME_UNSET)
    }

    override fun seekTo(mediaId: MediaId, pos: Duration?) {
        seekTo(browser?.indexOf(mediaId) ?: return, pos)
    }

    override fun seekTo(index: Int, pos: Duration?) {
        browser?.seekTo(index, pos?.inWholeMilliseconds ?: C.TIME_UNSET)
    }


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED)
            _currentPlayback.update {
                mediaItem?.mediaMetadata?.playback?.apply {
                    isPlayable.update { mediaId.uri?.isPlayable(context) ?: false }
                }
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


    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        when (val event = command.toMediaSessionEvent(args)) {
            is TimingDataUpdatedEvent -> {
                _currentPlayback.update {
                    it?.copy()?.apply {
                        timingData = event.timingData
                    }
                }
            }
        }

        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }
}


private fun MediaBrowser.indexOf(mediaId: MediaId): Int? {
    for (i in 0 until mediaItemCount) {
        if (getMediaItemAt(i).mediaId == mediaId.toString())
            return i
    }
    return null
}