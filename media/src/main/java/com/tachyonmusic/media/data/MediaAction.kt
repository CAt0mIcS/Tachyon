package com.tachyonmusic.media.data

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
object MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */
    const val SetPlaybackEvent = "com.tachyonmusic.SET_PLAYBACK"
    const val Playback = "Playback"  // use Bundle.putParcelable

    const val SetStartTimeEvent = "com.tachyonmusic.SET_START_TIME"
    const val StartTime = "StartTime"  // use Bundle.putLong

    const val SetEndTimeEvent = "com.tachyonmusic.SET_END_TIME"
    const val EndTime = "EndTime"  // use Bundle.putLong

    const val SendLoopsEvent = "com.tachyonmusic.SEND_LOOPS"
    const val Loops = "Loops"  // use Bundle.putParcelableArrayList

    const val SendPlaylistsEvent = "com.tachyonmusic.SEND_PLAYLISTS"
    const val Playlists =
        "Playlists"  // use Bundle.putParcelableArrayList

    const val RequestMediaSourceReloadEvent = "com.tachyonmusic.REQUEST_MEDIA_SOURCE_RELOAD"

    const val CombinePlaybackTypesChangedEvent = "com.tachyonmusic.COMBINE_PLAYBACK_TYPES_CHANGED"
    const val CombinePlaybackTypes = "CombinePlaybackTypes" // use Bundle.putBoolean

    const val RequestPlaybackUpdateEvent = "com.tachyonmusic.REQUEST_PLAYBACK_UPDATE"

    const val CurrentPlaylistIndexChangedEvent = "com.tachyonmusic.CURRENT_PLAYLIST_INDEX_CHANGED"
    const val CurrentPlaylistIndex = "CurrentPlaylistIndex" // use Bundle.putInt


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */
    const val OnPlaybackStateChangedEvent = "com.tachyonmusic.ON_PLAYBACK_STATE_CHANGED"
    const val IsPlaying = "IsPlaying"  // use Bundle.putBoolean

    //    const val SetPlaybackEvent = "com.tachyonmusic.SET_PLAYBACK"
    //    const val Playback = "Playback"  // use Bundle.putParcelable
}