package com.tachyonmusic.domain

import android.app.Activity
import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
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
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.data.ext.*
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MediaPlaybackServiceMediaBrowserController(
    private val userRepository: UserRepository
) : MediaBrowserController, DefaultLifecycleObserver, Player.Listener {

    private var browser: MediaBrowser? = null

    private val eventListeners = mutableListOf<MediaBrowserController.EventListener>()

    var onConnected: (() -> Unit)? = null

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
            onConnected?.invoke()
        }
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

    override fun addListener(listener: MediaBrowserController.EventListener) {
        eventListeners += listener
    }

    override fun removeListener(listener: MediaBrowserController.EventListener) {
        eventListeners -= listener
    }

    override fun getChildren(
        parentId: String,
        page: Int,
        pageSize: Int
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
        browser?.getChildren(parentId, page, pageSize, null) ?: Futures.immediateFuture(
            LibraryResult.ofItemList(listOf(), null)
        )

    override fun getPlaybacks(parentId: String, page: Int, pageSize: Int): List<Playback> =
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
    override val timingData: ArrayList<TimingData>?
        get() = browser?.mediaMetadata?.timingData
    override val currentPosition: Long?
        get() = browser?.currentPosition


    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val playback = mediaItem?.mediaMetadata?.playback
        for (listener in eventListeners)
            listener.onPlaybackTransition(playback)
    }
}














