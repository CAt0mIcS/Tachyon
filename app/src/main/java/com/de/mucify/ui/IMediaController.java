package com.de.mucify.ui;


public interface IMediaController {
    /**
     * Unpauses the paused audio. Expects that the Playback has already been started using start(String)
     */
    void unpause();

    /**
     * Pauses the currently playing audio. Crashes if the Playback hasn't been started yet.
     */
    void pause();

    /**
     * Seeks the currently playing audio to the specified offset. Crashes if the Playback hasn't been started yet.
     */
    void seekTo(int millis);

    /**
     * Plays new audio with specified MediaId. Afterwards all operations like seekTo, pause, isPlaying, ...
     * are safe to be called.
     */
    void play(String mediaId);

    /**
     * Starts the next song in either the playlist or the next one in the alphabet after the current one.
     * Loops back to the start if we're at the end
     */
    void next();

    /**
     * Starts the previous song in either the playlist or the previous one in the alphabet before the current one.
     * Loops back to the end if we're at the start
     */
    void previous();

    /**
     * Checks if the playback is playing. Crashes if the Playback hasn't been started yet.
     */
    boolean isPlaying();

    /**
     * Checks if the playback was set using start(String mediaId)
     * Crashes if the Playback hasn't been started yet.
     */
    boolean isCreated();

    /**
     * Checks if the playback is paused. Crashes if the Playback hasn't been started yet.
     */
    boolean isPaused();

    /**
     * Sets the start time of the currently playing playback
     *
     * @param millis offset from audio position zero.
     */
    void setStartTime(int millis);

    /**
     * Sets the end time of the currently playing playback
     *
     * @param millis offset from audio duration.
     */
    void setEndTime(int millis);

    /**
     * Saves current playback with current start and end time as loop with name loopName
     */
    void saveAsLoop(String loopName);

    /**
     * Gets the start time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    int getStartTime();

    /**
     * Gets the end time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    int getEndTime();

    /**
     * Gets the current position of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    int getCurrentPosition();

    /**
     * Gets the duration of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    int getDuration();

    /**
     * Gets the title of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    String getSongTitle();

    /**
     * Gets the artist of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    String getSongArtist();
}
