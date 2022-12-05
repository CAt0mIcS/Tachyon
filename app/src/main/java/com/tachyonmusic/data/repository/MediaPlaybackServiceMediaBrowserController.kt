package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.ComponentName
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.ListenableMutableList
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.media.data.ext.duration
import com.tachyonmusic.media.data.ext.name
import com.tachyonmusic.media.data.ext.playback
import com.tachyonmusic.media.data.ext.timingData
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MediaPlaybackServiceMediaBrowserController : MediaBrowserController,
    Player.Listener,
    ListenableMutableList.EventListener<TimingData>,
    IListenable<MediaBrowserController.EventListener> by Listenable() {

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
                MediaAction.setPlaybackEvent(browser!!, value)
            else
                stop()
        }

    override var playWhenReady: Boolean
        get() = browser?.playWhenReady ?: true
        set(value) {
            browser?.playWhenReady = value
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

    override fun seekTo(pos: Long) {
        browser?.seekTo(pos)
    }

    override fun getChildren(
        parentId: String,
        page: Int,
        pageSize: Int
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
        browser?.getChildren(parentId, page, pageSize, null) ?: Futures.immediateFuture(
            LibraryResult.ofItemList(listOf(), null)
        )

    override fun getPlaybacksNative(parentId: String, page: Int, pageSize: Int): List<Playback> =
        runBlocking {
            val children = getChildren(parentId, page, pageSize).await().value ?: emptyList()
            List(children.size) { i ->
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
    override var timingData: MutableList<TimingData>?
        get() {
            val data = browser?.mediaMetadata?.timingData
            return if (data == null) null
            else ListenableMutableList(data.timingData).apply {
                registerEventListener(this@MediaPlaybackServiceMediaBrowserController)
            }
        }
        set(value) {
            if (value != null)
                onChanged(value)
            else
                onChanged(listOf())
        }

    override val currentPosition: Long?
        get() = browser?.currentPosition


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val playback = mediaItem?.mediaMetadata?.playback
        invokeEvent {
            it.onPlaybackTransition(playback)
        }
    }

    // TODO: Only emit if it doesn't come from e.g. a seek which changes isPlaying to false and then to true quickly
    override fun onIsPlayingChanged(isPlaying: Boolean) = invokeEvent {
        it.onIsPlayingChanged(isPlaying)
    }

    override fun onChanged(list: List<TimingData>) {
        if (browser == null)
            return
        MediaAction.updateTimingDataEvent(
            browser!!,
            TimingDataController(list)
        )
    }
}