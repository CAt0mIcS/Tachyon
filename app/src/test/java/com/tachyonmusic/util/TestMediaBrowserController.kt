package com.tachyonmusic.util

import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController

class TestMediaBrowserController : MediaBrowserController {
    override var playback: Playback?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var playWhenReady: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun getChildren(
        parentId: String,
        page: Int,
        pageSize: Int
    ): LibraryResult<ImmutableList<MediaItem>> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlaybacksNative(
        parentId: String,
        page: Int,
        pageSize: Int
    ): List<Playback> {
        TODO("Not yet implemented")
    }

    override val isPlaying: Boolean
        get() = TODO("Not yet implemented")
    override val title: String?
        get() = TODO("Not yet implemented")
    override val artist: String?
        get() = TODO("Not yet implemented")
    override val name: String?
        get() = TODO("Not yet implemented")

    override var duration: Long? = 0L

    override var timingData: MutableList<TimingData>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var currentPosition: Long? = 0L

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun play() {
        TODO("Not yet implemented")
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun seekTo(pos: Long) {
        TODO("Not yet implemented")
    }

    override fun seekForward() {
        TODO("Not yet implemented")
    }

    override fun seekBack() {
        TODO("Not yet implemented")
    }

    override val listeners: MutableSet<MediaBrowserController.EventListener>
        get() = TODO("Not yet implemented")

    override fun registerEventListener(listener: MediaBrowserController.EventListener) {
        TODO("Not yet implemented")
    }

    override fun unregisterEventListener(listener: MediaBrowserController.EventListener) {
        TODO("Not yet implemented")
    }

    override fun invokeEvent(e: (MediaBrowserController.EventListener) -> Unit) {
        TODO("Not yet implemented")
    }
}