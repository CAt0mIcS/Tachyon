package com.tachyonmusic.domain

import android.content.ComponentName
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.constants.MediaAction
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.media.data.ext.playback
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.runBlocking

class MediaPlaybackServiceMediaBrowserController(
    private val userRepository: UserRepository
) : MediaBrowserController {

    private var browser: MediaBrowser? = null

    fun onCreate(owner: ComponentActivity): ListenableFuture<MediaBrowser> {
        // TODO: Does this need to be done in onStart/onResume?
        // TODO: Should we disconnect in onStop?

        val sessionToken = SessionToken(
            owner,
            ComponentName(owner, MediaPlaybackService::class.java)
        )

        return MediaBrowser.Builder(owner, sessionToken)
            .buildAsync()
    }

    fun set(browser: MediaBrowser) {
        this.browser = browser
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
            val children = getChildren(parentId, page, pageSize).await().value!!
            List(children.size) { i ->
                children[i].mediaMetadata.playback!!
            }
        }

}














