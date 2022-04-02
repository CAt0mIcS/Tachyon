package com.de.mucify.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.de.mucify.Util;
import com.de.mucify.service.MediaAction;
import com.de.mucify.service.MediaPlaybackService;
import com.de.mucify.service.MetadataKey;


public class MediaBrowserController implements IMediaController {
    private final MediaBrowserCompat mMediaBrowser;
    private final MediaControllerCallback mControllerCallback = new MediaControllerCallback();

    private final MediaControllerActivity mActivity;


    public MediaBrowserController(MediaControllerActivity activity) {
        mActivity = activity;

        // Not using startForegroundService because the service only has 5 seconds to call Service.startForeground
        // which doesn't happen until we actually start playing audio
        mActivity.startService(new Intent(mActivity, MediaPlaybackService.class));

        mMediaBrowser = new MediaBrowserCompat(mActivity,
                new ComponentName(mActivity, MediaPlaybackService.class),
                new ConnectionCallback(),
                null);

        Log.d("Mucify", "MediaBrowserController created");
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    public void onStart() {
        mMediaBrowser.connect();
        Log.d("Mucify", "Started connecting to MediaPlaybackService");
    }

    public void onStop() {
        if (MediaControllerCompat.getMediaController(mActivity) != null) {
            MediaControllerCompat.getMediaController(mActivity).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
        Log.d("Mucify", "Started disconnecting from MediaPlaybackService");
    }

    /**
     * Unpauses the paused audio. Expects that the Playback has already been started using start(String)
     */
    @Override
    public void unpause() {
        play();
    }

    /**
     * Pauses the currently playing audio. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public void pause() {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().pause();
    }

    /**
     * Seeks the currently playing audio to the specified offset. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public void seekTo(int millis) {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().seekTo(millis);
    }

    /**
     * Sets the current playback to the media id specified. Doesn't start playback
     */
    @Override
    public void setMediaId(String mediaId) {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().playFromMediaId(mediaId, null);
    }

    /**
     * @return media id of current playback (song/loop/playlist)
     */
    @Override
    public String getMediaId() {
        return getMetadata().getString(MetadataKey.MediaId);
    }

    /**
     * Starts playback, requires that media id was already set
     */
    @Override
    public void play() {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().play();
    }

    /**
     * Starts the next song in either the playlist or the next one in the alphabet after the current one.
     * Loops back to the start if we're at the end
     */
    @Override
    public void next() {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().skipToNext();
        for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
            c.onPlay();
    }

    /**
     * Starts the previous song in either the playlist or the previous one in the alphabet before the current one.
     * Loops back to the end if we're at the start
     */
    @Override
    public void previous() {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().skipToPrevious();
        for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
            c.onPlay();
    }


    /**
     * Checks if the playback state is equal to playing. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public boolean isPlaying() {
        return MediaControllerCompat.getMediaController(mActivity).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    /**
     * Checks if the playback state is not none, in which case the Playback is assumed to be created.
     * Crashes if the Playback hasn't been started yet.
     */
    @Override
    public boolean isCreated() {
        int state = MediaControllerCompat.getMediaController(mActivity).getPlaybackState().getState();
        return state != PlaybackStateCompat.STATE_NONE;
    }

    /**
     * Checks if the playback state is equal to paused. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public boolean isPaused() {
        return MediaControllerCompat.getMediaController(mActivity).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED;
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService.
     * Crashes if the Playback hasn't been started yet.
     *
     * @param millis offset from audio position zero.
     */
    @Override
    public void setStartTime(int millis) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaAction.StartTime, millis);
        sendCustomAction(MediaAction.SetStartTime, bundle);
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService.
     * Crashes if the Playback hasn't been started yet.
     *
     * @param millis offset from audio duration.
     */
    @Override
    public void setEndTime(int millis) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaAction.EndTime, millis);
        sendCustomAction(MediaAction.SetEndTime, bundle);
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService. Saves the current playback
     * as a loop with loopName as name.
     */
    @Override
    public void saveAsLoop(String loopName) {
        Bundle bundle = new Bundle();
        bundle.putString(MediaAction.LoopName, loopName);
        sendCustomAction(MediaAction.SaveAsLoop, bundle);
    }

    /**
     * Gets the start time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public int getStartTime() {
        return (int) getMetadata().getLong(MetadataKey.StartPos);
    }

    /**
     * Gets the end time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public int getEndTime() {
        return (int) getMetadata().getLong(MetadataKey.EndPos);
    }

    /**
     * @return the image associated with the album of the current playing playback
     */
    @Override
    public Bitmap getImage() {
        return getMetadata().getBitmap(MetadataKey.AlbumArt);
    }

    /**
     * @return the name of the currently playing playlist, or null if no playlist is playing
     */
    @Override
    public String getPlaylistName() {
        return getMetadata().getString(MetadataKey.PlaylistName);
    }

    /**
     * @return the media id of the currently playing song in the current playlist, or null if no playlist is playing
     */
    @Override
    public String getCurrentSongMediaId() {
        return getMetadata().getString(MetadataKey.CurrentSongMediaId);
    }

    /**
     * @return the number of songs in the playlist, or null if no playlist is playing
     */
    @Override
    public int getSongCountInPlaylist() {
        return (int) getMetadata().getLong(MetadataKey.SongCountInPlaylist);
    }

    /**
     * @return the total duration of all songs in the playlist in milliseconds, or null if no
     * playlist is playing
     */
    @Override
    public int getTotalPlaylistLength() {
        return (int) getMetadata().getLong(MetadataKey.TotalPlaylistLength);
    }

    /**
     * If we're currently playing a playlist. We'll skip to the audio specified by the mediaId.
     * If we're not playing a playlist, nothing will happen
     */
    @Override
    public void skipToPlaylistSong(String mediaId) {
        Bundle bundle = new Bundle();
        bundle.putString(MediaAction.MediaId, mediaId);
        sendCustomAction(MediaAction.ChangePlaybackInPlaylist, bundle);
    }

    /**
     * Gets the current position of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public int getCurrentPosition() {
        return (int) getPlaybackState().getPosition();
    }

    /**
     * Gets the duration of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public int getDuration() {
        return (int) getMetadata().getLong(MetadataKey.Duration);
    }

    /**
     * Gets the title of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    @Override
    public String getSongTitle() {
        return getMetadata().getString(MetadataKey.Title);
    }

    /**
     * Gets the artist of the currently playing song. Metadata must've been set, otherwise the
     * function will crash.
     */
    @Override
    public String getSongArtist() {
        return getMetadata().getString(MetadataKey.Artist);
    }

    /**
     * @return playback state which was set in MediaPlaybackService
     */
    public PlaybackStateCompat getPlaybackState() {
        return MediaControllerCompat.getMediaController(mActivity).getPlaybackState();
    }

    /**
     * @return metadata which was set in the MediaPlaybackService
     */
    public MediaMetadataCompat getMetadata() {
        return MediaControllerCompat.getMediaController(mActivity).getMetadata();
    }

    public void sendCustomAction(String action, Bundle bundle) {
        MediaControllerCompat.getMediaController(mActivity).getTransportControls().sendCustomAction(action, bundle);
    }


    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

            // Create a MediaControllerCompat
            MediaControllerCompat mediaController =
                    new MediaControllerCompat(mActivity, token);

            // Save the controller
            MediaControllerCompat.setMediaController(mActivity, mediaController);

            // Finish building the UI
            mActivity.onConnected();

            // Register a Callback to stay in sync
            MediaControllerCompat.getMediaController(mActivity).registerCallback(mControllerCallback);
            Log.d("Mucify", "MediaBrowserController connection established");
        }

        @Override
        public void onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d("Mucify", "MediaControllerActivity connection suspended");
        }

        @Override
        public void onConnectionFailed() {
            // The Service has refused our connection
            Log.d("Mucify", "MediaControllerActivity connection failed");
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onSessionEvent(String event, Bundle extras) {
            switch (event) {
                case MediaAction.OnPlay:
                    for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
                        c.onPlay();
                    break;
                case MediaAction.OnPause:
                    for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
                        c.onPause();
                    break;
                case MediaAction.OnSeekTo:
                    for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
                        c.onSeekTo(extras.getInt(MediaAction.SeekPos));
                    break;
                case MediaAction.OnMediaIdChanged:
                    for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
                        c.onMediaIdChanged(extras.getString(MediaAction.MediaId));
                    break;
                case MediaAction.OnPlaybackInPlaylistChanged:
                    for (MediaControllerActivity.Callback c : mActivity.getCallbacks())
                        c.onPlaybackInPlaylistChanged(extras.getString(MediaAction.MediaId));
                    break;
                default:
                    Util.logGlobal("Unsupported session event " + event);
            }
        }

        @Override
        public void onSessionDestroyed() {
            Log.d("Mucify", "MediaControllerActivity session destroyed");
            mMediaBrowser.disconnect();
            // maybe schedule a reconnection using a new MediaBrowser instance
            mActivity.onDisconnected();
        }
    }

}
