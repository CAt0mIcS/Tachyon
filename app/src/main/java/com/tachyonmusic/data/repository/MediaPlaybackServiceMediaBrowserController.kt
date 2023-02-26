package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.media.util.duration
import com.tachyonmusic.media.util.name
import com.tachyonmusic.media.util.playback
import com.tachyonmusic.media.util.timingData
import com.tachyonmusic.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MediaPlaybackServiceMediaBrowserController : MediaBrowserController, Player.Listener,
    MediaBrowser.Listener, IListenable<MediaBrowserController.EventListener> by Listenable() {

    var browser: MediaBrowser? = null

    /**
     * We might want to seek to the position while the player is still preparing. Cache
     * the position and seek to it in [onMediaItemTransition]
     */
    private var cachedSeekPositionWhenAvailable: Duration? = null

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

            _playbackState.update { playback }
            _playWhenReadyState.update { playWhenReady }
            _timingDataState.update { timingData }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        browser?.release()
    }

    override var playback: SinglePlayback?
        get() = browser?.currentMediaItem?.mediaMetadata?.playback
        set(value) {
            browser?.dispatchMediaEvent(SetPlaybackEvent(value))
            _associatedPlaylistState.update { null }
        }

    override var playWhenReady: Boolean
        get() = browser?.playWhenReady ?: true
        set(value) {
            browser?.playWhenReady = value
        }

    private val _playbackState = MutableStateFlow<SinglePlayback?>(null)
    override val playbackState = _playbackState.asStateFlow()

    private val _associatedPlaylistState = MutableStateFlow<Playlist?>(null)
    override val associatedPlaylistState = _associatedPlaylistState.asStateFlow()

    private val _playWhenReadyState = MutableStateFlow(false)
    override val playWhenReadyState = _playWhenReadyState.asStateFlow()

    private val _sortParamsState = MutableStateFlow(SortParameters())
    override val sortParamsState = _sortParamsState.asStateFlow()

    override var sortParams: SortParameters
        get() = sortParamsState.value
        set(value) {
            browser?.dispatchMediaEvent(SetSortingParamsEvent(value)) ?: return
            _sortParamsState.update { value }
        }

    override fun playPlaylist(playlist: Playlist?) {
        _playbackState.update { null }
        _associatedPlaylistState.update { playlist }
        browser?.dispatchMediaEvent(SetPlaybackEvent(playlist))
    }

    override var repeatMode: RepeatMode
        get() = repeatModeState.value
        set(value) {
            if (browser != null) {
                browser!!.dispatchMediaEvent(SetRepeatModeEvent(value))
                _repeatModeState.update { value }
            }
        }

    private val _repeatModeState = MutableStateFlow<RepeatMode>(RepeatMode.All)
    override val repeatModeState = _repeatModeState.asStateFlow()

    override val nextMediaItemIndex: Int
        get() = browser?.nextMediaItemIndex ?: C.INDEX_UNSET

    override fun prepare() {
        browser?.prepare()
    }

    override fun stop() {
        browser?.stop()
    }

    override fun play() {
        browser?.play()
    }

    override fun pause() {
        browser?.pause()
    }

    override fun seekTo(pos: Duration?) {
        if (browser?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) != true) {
            cachedSeekPositionWhenAvailable = pos
        } else {
            browser?.seekTo(pos?.inWholeMilliseconds ?: C.TIME_UNSET)
        }
    }

    override fun seekForward() {
        browser?.seekForward()
    }

    override fun seekBack() {
        browser?.seekBack()
    }

    override fun seekTo(playback: SinglePlayback, pos: Duration?) {
        val i = associatedPlaylistState.value?.playbacks?.indexOfOrNull(playback)
            ?: throw IllegalArgumentException("playback not in playlist")

        browser?.seekTo(i, pos?.inWholeMilliseconds ?: C.TIME_UNSET)
    }

    override suspend fun getChildren(
        parentId: String,
        page: Int,
        pageSize: Int
    ): LibraryResult<ImmutableList<MediaItem>> =
        browser?.getChildren(parentId, page, pageSize, null)?.await()
            ?: LibraryResult.ofItemList(listOf(), null)


    override suspend fun getPlaybacksNative(
        parentId: String,
        page: Int,
        pageSize: Int
    ): List<Playback> {
        val children = getChildren(parentId, page, pageSize).value ?: emptyList()
        return List(children.size) { i ->
            children[i].mediaMetadata.playback!!
        }
    }

    override val isPlaying: Boolean
        get() = browser?.isPlaying ?: false
    override val title: String?
        get() = browser?.mediaMetadata?.title as String?
    override val artist: String?
        get() = browser?.mediaMetadata?.artist as String?
    override val name: String?
        get() = browser?.mediaMetadata?.name
    override val duration: Duration?
        get() = browser?.mediaMetadata?.duration
    override var timingData: TimingDataController?
        get() = browser?.mediaMetadata?.timingData
        set(value) {
            if (value == null)
                throw IllegalArgumentException("TimingDataController mustn't be null")
            browser?.dispatchMediaEvent(SetTimingDataEvent(value))
            _timingDataState.update { value }
        }

    override val currentPosition: Duration?
        get() = if (browser?.currentMediaItem == null) null else cachedSeekPositionWhenAvailable
            ?: browser?.currentPosition?.ms

    private val _timingDataState = MutableStateFlow<TimingDataController?>(null)
    override val timingDataState = _timingDataState.asStateFlow()


    /***********************************************************************************************
     * [Player.Listener]
     **********************************************************************************************/

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val playback = mediaItem?.mediaMetadata?.playback
        _playbackState.update { playback }
        _timingDataState.update { playback?.timingData }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        _playWhenReadyState.update { playWhenReady }
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        if (cachedSeekPositionWhenAvailable != null &&
            availableCommands.contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
        ) {
            seekTo(cachedSeekPositionWhenAvailable ?: return)
            cachedSeekPositionWhenAvailable = null
        }
    }

    /***********************************************************************************************
     * [MediaBrowser.Listener]
     **********************************************************************************************/

    override fun onDisconnected(controller: MediaController) {
        _playbackState.update { null }
        _playWhenReadyState.update { false }
        _timingDataState.update { null }
        _associatedPlaylistState.update { null }
        cachedSeekPositionWhenAvailable = null
    }

    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> = future(Dispatchers.Main) {
        when (val event = command.toMediaSessionEvent(args)) {
            is TimingDataUpdatedEvent -> {
                _timingDataState.update {
                    val new = event.timingData ?: return@update null
                    TimingDataController(new.timingData, new.currentIndex)
                }
            }
        }

        SessionResult(SessionResult.RESULT_SUCCESS)
    }
}

private val MediaBrowser.mediaItems: List<MediaItem>
    get() = List(mediaItemCount) {
        getMediaItemAt(it)
    }