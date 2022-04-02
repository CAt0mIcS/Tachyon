package com.de.mucify.service;

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
public class MediaAction {
    /**
     * Events sent to the MediaPlaybackService by the MediaBrowserController
     */

    public static final String SetStartTime = "com.de.mucify.SET_START_TIME";
    public static final String StartTime = "StartTime";

    public static final String SetEndTime = "com.de.mucify.SET_END_TIME";
    public static final String EndTime = "EndTime";

    public static final String CastStarted = "com.de.mucify.CAST_STARTED";

    public static final String SaveAsLoop = "com.de.mucify.SAVE_AS_LOOP";
    public static final String LoopName = "LoopName";

    public static final String ChangePlaybackInPlaylist = "com.de.mucify.SKIP_TO_SONG_IN_PLAYLIST";
    public static final String MediaId = "MediaId";


    /**
     * Events sent to the MediaBrowserController by the MediaPlaybackService
     */

    public static final String OnPlay = "com.de.mucify.ON_PLAY";

    public static final String OnPause = "com.de.mucify.ON_PAUSE";

    public static final String OnMediaIdChanged = "com.de.mucify.ON_MEDIA_ID_CHANGED";
    // MediaId

    public static final String OnPlaybackInPlaylistChanged = "com.de.mucify.ON_PLAYBACK_IN_PLAYLIST_CHANGED";
    // MediaId

    public static final String OnSeekTo = "com.de.mucify.ON_SEEK_TO";
    public static final String SeekPos = "SeekPos";

    public static final String OnStop = "com.de.mucify.ON_STOP";
}
