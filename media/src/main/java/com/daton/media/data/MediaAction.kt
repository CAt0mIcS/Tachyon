package com.daton.media.data

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    const val SetMediaId = "com.daton.mucify.SET_MEDIA_ID"
    const val MediaId = "MediaId"

    const val SetStartTime = "com.daton.mucify.SET_START_TIME"
    const val StartTime = "StartTime"

    const val SetEndTime = "com.daton.mucify.SET_END_TIME"
    const val EndTime = "EndTime"

    const val StoragePermissionChanged = "com.daton.mucify.STORAGE_PERMISSION_CHANGED"
    const val StoragePermissionGranted = "StoragePermissionGranted"

    const val SendLoops = "com.daton.mucify.SEND_LOOPS"
    const val Loops = "Loops"

    const val SendPlaylists = "com.daton.mucify.SEND_PLAYLISTS"
    const val Playlists = "Playlists"


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */
    const val MediaIdChanged = "com.daton.mucify.MEDIA_ID_CHANGED"

}