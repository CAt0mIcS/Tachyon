package com.daton.media

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    const val SetMediaId = "com.de.mucify.SET_MEDIA_ID"
    const val MediaId = "MediaId"

    const val SetStartTime = "com.de.mucify.SET_START_TIME"
    const val StartTime = "StartTime"

    const val SetEndTime = "com.de.mucify.SET_END_TIME"
    const val EndTime = "EndTime"
}