package com.daton.media.data

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    const val SetPlaybackEvent = "com.daton.mucify.SET_PLAYBACK"
    const val Playback = "Playback"  // use Bundle.putParcelable

    const val SetStartTimeEvent = "com.daton.mucify.SET_START_TIME"
    const val StartTime = "StartTime"  // use Bundle.putLong

    const val SetEndTimeEvent = "com.daton.mucify.SET_END_TIME"
    const val EndTime = "EndTime"  // use Bundle.putLong

    const val SendLoopsEvent = "com.daton.mucify.SEND_LOOPS"
    const val Loops = "Loops"  // use Bundle.putParcelableArrayList

    const val SendPlaylistsEvent = "com.daton.mucify.SEND_PLAYLISTS"
    const val Playlists =
        "Playlists"  // use Bundle.putParcelableArrayList

    const val RequestMediaSourceReloadEvent = "com.daton.mucify.REQUEST_MEDIA_SOURCE_RELOAD"

    const val CombinePlaybackTypesChangedEvent = "com.daton.mucify.COMBINE_PLAYBACK_TYPES_CHANGED"
    const val CombinePlaybackTypes = "CombinePlaybackTypes" // use putBoolean

    const val RequestPlaybackUpdateEvent = "com.daton.mucify.REQUEST_PLAYBACK_UPDATE"


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */
    const val OnPlaybackStateChangedEvent = "com.daton.mucify.ON_PLAYBACK_STATE_CHANGED"
    const val IsPlaying = "IsPlaying"  // use Bundle.putBoolean

    //    const val SetPlaybackEvent = "com.daton.mucify.SET_PLAYBACK"
//    const val Playback = "Playback"  // use Bundle.putParcelable
}