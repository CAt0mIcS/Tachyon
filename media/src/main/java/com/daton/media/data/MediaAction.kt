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
    const val MediaId = "MediaId"  // use Bundle.putString

    const val SetStartTime = "com.daton.mucify.SET_START_TIME"
    const val StartTime = "StartTime"  // use Bundle.putLong

    const val SetEndTime = "com.daton.mucify.SET_END_TIME"
    const val EndTime = "EndTime"  // use Bundle.putLong

    const val StoragePermissionChanged = "com.daton.mucify.STORAGE_PERMISSION_CHANGED"
    const val StoragePermissionGranted = "StoragePermissionGranted"  // use Bundle.putBoolean

    const val SendLoops = "com.daton.mucify.SEND_LOOPS"
    const val Loops = "Loops"  // use Bundle.putStringArray with Json.encodeToString(loop)

    const val SendPlaylists = "com.daton.mucify.SEND_PLAYLISTS"
    const val Playlists =
        "Playlists"  // use Bundle.putStringArray with Json.encodeToString(playlist)


    const val CombinePlaybackTypesChanged = "com.daton.mucify.COMBINE_PLAYBACK_TYPES_CHANGED"
    const val CombinePlaybackTypes = "CombinePlaybackTypes"


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */
    const val MediaIdChanged = "com.daton.mucify.MEDIA_ID_CHANGED"

    const val OnPlaybackStateChanged = "com.daton.mucify.ON_PLAYBACK_STATE_CHANGED"
    const val IsPlaying = "IsPlaying"  // use Bundle.putBoolean
}