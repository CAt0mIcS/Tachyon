package com.de.mucify.service;

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
public class MediaAction {
    public static final String SetStartTime = "com.de.mucify.SET_START_TIME";
    public static final String StartTime = "StartTime";

    public static final String SetEndTime = "com.de.mucify.SET_END_TIME";
    public static final String EndTime = "EndTime";

    public static final String CastStarted = "com.de.mucify.CAST_STARTED";
}
