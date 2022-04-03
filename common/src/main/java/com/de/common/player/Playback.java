package com.de.common.player;


import android.content.Context;

import java.io.File;

public abstract class Playback {
    /**
     * Starts or resumes playback.
     * If playback had previously been paused, playback will continue from where it was paused.
     * If playback had been stopped, or never started before, playback will start at the beginning.
     */
    public abstract void start(Context context);

    /**
     * Pauses playback. Call start(Context) to resume.
     */
    public abstract void pause();

    /**
     * Checks whether the MediaPlayer is playing.
     */
    public abstract boolean isPlaying();

    /**
     * Checks whether the MediaPlayer is not playing.
     */
    public boolean isPaused() {
        return !isPlaying();
    }

    /**
     * Seeks to specified time position
     *
     * @param millis position to seek to in milliseconds
     */
    public abstract void seekTo(int millis);


    /**
     * Gets the duration of the file.
     */
    public abstract int getDuration();

    /**
     * Gets the current playback position.
     */
    public abstract int getCurrentPosition();

    /**
     * Stops playback after playback has been started or paused.
     */
    public abstract void stop();

    /**
     * Releases resources associated with this MediaPlayer object.
     * Should be called whenever an Activity of an application is paused (its onPause() method is called),
     * or stopped (its onStop() method is called).
     */
    public abstract void reset();

    /**
     * Prepares MediaPlayer resources. Needs to be called again after reset() has been called
     */
    public abstract void create(Context context);

    /**
     * Gets the next Song in the list. If called on a Song, the next item will be determined
     * by the order of MediaLibrary.AvailableSongs.
     * If called on a Playlist, the next Song will be created.
     *
     * @return Fully created playback which can be started immediately.
     */
    public abstract Playback next(Context context);

    /**
     * Gets the previous Song in the list. If called on a Song, the previous item will be determined
     * by the order of MediaLibrary.AvailableSongs.
     * If called on a Playlist, the previous Song will be created.
     *
     * @return Fully created playback which can be started immediately.
     */
    public abstract Playback previous(Context context);

    /**
     * @return Current active song, only useful for Playlist. In Song, instance itself is returned.
     */
    public abstract Song getCurrentSong();

    /**
     * @return either the path to the song, loop, or playlist file
     */
    public abstract File getPath();

    /**
     * Sets the volume for this media player and the current Song in the playlist, if one is running.
     * 1.0f means 100% volume.
     */
    public abstract void setVolume(float left, float right);

    /**
     * Checks if the MediaPlayer is ready to be started.
     */
    public abstract boolean isCreated();

    /**
     * @return The title of the current song
     */
    public abstract String getTitle();

    /**
     * @return The subtitle (usually artist) of the current song
     */
    public abstract String getSubtitle();

    /**
     * MediaIds are constructed with the type of Playback (Song_, Loop_, Playlist_) and the path
     * to either the song, loop, or playlist file.
     *
     * @return The media id of the current playback
     */
    public abstract String getMediaId();
}
