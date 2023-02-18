package com.tachyonmusic.core.data.constants

import android.os.Bundle
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import com.tachyonmusic.core.domain.TimingDataController
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
    val setTimingDataCommand =
        SessionCommand("com.tachyonmusic.UPDATE_TIMING_DATA", Bundle.EMPTY)
    val setRepeatModeCommand =
        SessionCommand("com.tachyonmusic.REPEAT_MODE_CHANGED", Bundle.EMPTY)

    fun MediaBrowser.sendSetPlaybackEvent(playback: Playback?) =
        sendCustomCommand(setPlaybackCommand, Bundle().apply {
            putParcelable(MetadataKeys.Playback, playback)
        })

    fun MediaBrowser.sendSetTimingDataEvent(timingData: TimingDataController) =
        sendCustomCommand(setTimingDataCommand, Bundle().apply {
            putParcelable(MetadataKeys.TimingData, timingData)
        })

    fun MediaBrowser.sendSetRepeatModeEvent(repeatMode: RepeatMode) {
        sendCustomCommand(setRepeatModeCommand, Bundle().apply {
            putInt(MetadataKeys.RepeatMode, repeatMode.id)
        })
    }


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */
    val timingDataUpdatedCommand =
        SessionCommand("com.tachyonmusic.TIMING_DATA_ADVANCED", Bundle.EMPTY)

    fun MediaSession.sendOnTimingDataUpdatedEvent(controller: TimingDataController?) {
        broadcastCustomCommand(timingDataUpdatedCommand, Bundle().apply {
            putParcelable(MetadataKeys.TimingData, controller)
        })
    }
}