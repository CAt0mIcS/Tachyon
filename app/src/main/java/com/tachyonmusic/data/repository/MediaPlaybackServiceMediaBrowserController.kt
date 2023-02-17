package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.data.constants.MediaAction
import com.tachyonmusic.core.data.constants.MediaAction.sendSetPlaybackEvent
import com.tachyonmusic.core.data.constants.MediaAction.sendSetRepeatModeEvent
import com.tachyonmusic.core.data.constants.MediaAction.sendSetTimingDataEvent
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.RepeatMode
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.data.ext.associatedPlaylist
import com.tachyonmusic.media.data.ext.duration
import com.tachyonmusic.media.data.ext.name
import com.tachyonmusic.media.data.ext.playback
import com.tachyonmusic.media.data.ext.timingData
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.future
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

class MediaPlaybackServiceMediaBrowserController : MediaBrowserController,
    Player.Listener,
    MediaBrowser.Listener,
    IListenable<MediaBrowserController.EventListener> by Listenable() {

    private var browser: MediaBrowser? = null

    /**
     * We might want to seek to the position while the player is still preparing. Cache
     * the position and seek to it in [onMediaItemTransition]
     */
    private var cachedSeekPositionWhenAvailable: Long? = null

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

    override var playback: Playback?
        get() = browser?.currentMediaItem?.mediaMetadata?.playback
        set(value) {
            if (value != null && browser != null)
                browser!!.sendSetPlaybackEvent(value)
            else
                stop()
        }

    override var playWhenReady: Boolean
        get() = browser?.playWhenReady ?: true
        set(value) {
            browser?.playWhenReady = value
        }

    override var repeatMode: RepeatMode = RepeatMode.One
        set(value) {
            if(browser != null) {
                field = value
                browser!!.sendSetRepeatModeEvent(repeatMode)
            }
        }

    override val nextMediaItemIndex: Int
        get() = browser?.nextMediaItemIndex ?: C.INDEX_UNSET

    override fun stop() {
        browser?.stop()
    }

    override fun play() {
        browser?.play()
    }

    override fun pause() {
        browser?.pause()
    }

    override fun seekTo(pos: Long) {
        if (browser?.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM) != true)
            cachedSeekPositionWhenAvailable = pos
        else
            browser?.seekTo(pos)
    }

    override fun seekForward() {
        browser?.seekForward()
    }

    override fun seekBack() {
        browser?.seekBack()
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
    override val duration: Long?
        get() = browser?.mediaMetadata?.duration
    override var timingData: TimingDataController?
        get() = browser?.mediaMetadata?.timingData
        set(value) {
            if (value == null)
                throw IllegalArgumentException("TimingDataController mustn't be null")
            browser?.sendSetTimingDataEvent(value)
        }

    override val currentPosition: Long?
        get() = if (browser?.currentMediaItem == null) null else browser?.currentPosition


    /***********************************************************************************************
     * [Player.Listener]
     **********************************************************************************************/

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val playback = mediaItem?.mediaMetadata?.playback
        val playlist = mediaItem?.mediaMetadata?.associatedPlaylist
        invokeEvent {
            it.onPlaybackTransition(playback, playlist)
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) = invokeEvent {
        it.onIsPlayingChanged(playWhenReady)
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        if (cachedSeekPositionWhenAvailable != null &&
            availableCommands.contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
        ) {
            browser?.seekTo(cachedSeekPositionWhenAvailable ?: 0)
            cachedSeekPositionWhenAvailable = null
        }
    }


    /***********************************************************************************************
     * [MediaBrowser.Listener]
     **********************************************************************************************/

    override fun onCustomCommand(
        controller: MediaController,
        command: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> = future(Dispatchers.Main) {
        when(command) {
            MediaAction.timingDataAdvancedCommand -> {
                invokeEvent {
                    it.onTimingDataAdvanced(args.getInt(MetadataKeys.TimingData))
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