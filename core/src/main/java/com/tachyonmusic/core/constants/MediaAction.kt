package com.tachyonmusic.core.constants

import android.os.Bundle
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.domain.playback.Playback

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    fun setPlaybackEvent(playback: Playback?) =
        SessionCommand("com.tachyonmusic.SET_PLAYBACK", Bundle().apply {
            putParcelable(MetadataKeys.Playback, playback)
        })

    fun setStartTimeEvent(startTime: Long) =
        SessionCommand("com.tachyonmusic.SET_START_TIME", Bundle().apply {
            putLong(MetadataKeys.StartTime, startTime)
        })

    fun setEndTimeEvent(endTime: Long) =
        SessionCommand("com.tachyonmusic.SET_END_TIME", Bundle().apply {
            putLong(MetadataKeys.EndTime, endTime)
        })
}