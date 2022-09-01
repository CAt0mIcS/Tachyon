package com.tachyonmusic.domain

import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.playback.Playback

interface MediaBrowserController {

    var playback: Playback?

    var playWhenReady: Boolean

    fun getChildren(
        parentId: String,
        page: Int = 0,
        pageSize: Int = Int.MAX_VALUE
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>

    fun getPlaybacks(parentId: String, page: Int = 0, pageSize: Int = Int.MAX_VALUE): List<Playback>

    val isPlaying: Boolean

    val title: String?
    val artist: String?
    val name: String?
    val duration: Long?
    val timingData: ArrayList<TimingData>?
    val currentPosition: Long?

    fun stop()
    fun play()
    fun pause()

    fun addListener(listener: EventListener)
    fun removeListener(listener: EventListener)

    interface EventListener {
        fun onPlaybackTransition(playback: Playback?) {}
    }
}