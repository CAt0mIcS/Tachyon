package com.tachyonmusic.core.constants

import android.os.Bundle
import androidx.media3.session.MediaBrowser
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
    val setPlaybackCommand = SessionCommand("com.tachyonmusic.SET_PLAYBACK", Bundle.EMPTY)

    fun setPlaybackEvent(browser: MediaBrowser, playback: Playback?) =
        browser.sendCustomCommand(setPlaybackCommand, Bundle().apply {
            putParcelable(MetadataKeys.Playback, playback)
        })

    fun setStartTimeEvent(browser: MediaBrowser, startTime: Long) =
        browser.sendCustomCommand(
            SessionCommand("com.tachyonmusic.SET_START_TIME", Bundle.EMPTY),
            Bundle().apply {
                putLong(MetadataKeys.StartTime, startTime)
            })

    fun setEndTimeEvent(browser: MediaBrowser, endTime: Long) =
        browser.sendCustomCommand(
            SessionCommand("com.tachyonmusic.SET_END_TIME", Bundle.EMPTY),
            Bundle().apply {
                putLong(MetadataKeys.EndTime, endTime)
            })
}