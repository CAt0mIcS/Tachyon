package com.de.mucify.service;

/**
 * Defines custom actions sent to the MediaPlaybackService. The first item is the event name the ones
 * below are required arguments. An empty line separates the different events
 */
public class MediaAction {
    public static final String SetStartTime = "com.de.mucify.set_start_time";
    public static final String StartTime = "StartTime";

    public static final String SetEndTime = "com.de.mucify.set_end_time";
    public static final String EndTime = "EndTime";
}
