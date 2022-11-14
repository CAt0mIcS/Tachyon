package com.tachyonmusic.core.constants

import android.os.Bundle
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.data.playback.Playback

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    val setPlaybackCommand = SessionCommand("com.tachyonmusic.SET_PLAYBACK", Bundle.EMPTY)
    val updateTimingDataCommand =
        SessionCommand("com.tachyonmusic.UPDATE_TIMING_DATA", Bundle.EMPTY)

    fun setPlaybackEvent(browser: MediaBrowser, playback: Playback?) =
        browser.sendCustomCommand(setPlaybackCommand, Bundle().apply {
            putParcelable(MetadataKeys.Playback, playback)
        })

    fun updateTimingDataEvent(browser: MediaBrowser, timingData: TimingDataController) =
        browser.sendCustomCommand(updateTimingDataCommand, Bundle().apply {
            putParcelable(MetadataKeys.TimingData, timingData)
        })
}