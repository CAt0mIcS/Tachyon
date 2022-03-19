package com.de.mucify.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telecom.Call;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.PermissionManager;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaAction;
import com.de.mucify.service.MediaPlaybackService;
import com.de.mucify.service.MetadataKey;

import java.util.ArrayList;
import java.util.List;

import kotlin.Metadata;


public abstract class MediaControllerActivity extends CastActivity {
    private MediaBrowserCompat mMediaBrowser;
    private final MediaControllerCallback mControllerCallback = new MediaControllerCallback();
    private final ArrayList<Callback> mCallbacks = new ArrayList<>();

    private MediaMetadataCompat mPreviousMetadata;
    private PlaybackStateCompat mPreviousPlaybackState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MY_TODO: Better waiting and figure out what to do if permission not granted
        PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // Not using startForegroundService because the service only has 5 seconds to call Service.startForeground
        // which doesn't happen until we actually start playing audio
        startService(new Intent(this, MediaPlaybackService.class));

        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaPlaybackService.class),
                new ConnectionCallback(),
                null);

        Log.d("Mucify", "MediaControllerActivity created");
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
        Log.d("Mucify", "Started connecting to MediaController");
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        Log.d("Mucify", "MediaControllerActivity.onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
        Log.d("Mucify", "Started disconnecting from MediaController");
    }

    public void addCallback(Callback c) {
        mCallbacks.add(c);
    }
    public void removeCallback(Callback c) {
        mCallbacks.remove(c);
    }

    /**
     * Unpauses the paused audio. Expects that the Playback has already been started using start(String)
     */
    public void unpause() {
        MediaControllerCompat.getMediaController(this).getTransportControls().play();
    }

    /**
     * Pauses the currently playing audio. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public void pause() {
        MediaControllerCompat.getMediaController(this).getTransportControls().pause();
    }

    /**
     * Seeks the currently playing audio to the specified offset. Crashes if the Playback hasn't been started yet.
     */
    public void seekTo(int millis) {
        MediaControllerCompat.getMediaController(this).getTransportControls().seekTo(millis);
    }

    /**
     * Plays new audio with specified MediaId. Afterwards all operations like seekTo, pause, isPlaying, ...
     * are safe to be called.
     */
    @Override
    public void play(String mediaId) {
        super.play(mediaId);
        MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(mediaId, null);
    }


    /**
     * Checks if the playback state is equal to playing. Crashes if the Playback hasn't been started yet.
     */
    @Override
    public boolean isPlaying() {
        return MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    /**
     * Checks if the playback state is not none, in which case the Playback is assumed to be created.
     * Crashes if the Playback hasn't been started yet.
     */
    public boolean isCreated() {
        int state = MediaControllerCompat.getMediaController(this).getPlaybackState().getState();
        return state != PlaybackStateCompat.STATE_NONE;
    }

    /**
     * Checks if the playback state is equal to paused. Crashes if the Playback hasn't been started yet.
     */
    public boolean isPaused() {
        return MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED;
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService. Only sets the start time
     * if the currently playing Playback is neither Loop nor Playlist, does nothing otherwise.
     * Crashes if the Playback hasn't been started yet.
     * @param millis offset from audio position zero.
     */
    public void setStartTime(int millis) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaAction.StartTime, millis);
        MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction(MediaAction.SetStartTime, bundle);
    }

    /**
     * Uses a custom event to call onCustomEvent in MediaPlaybackService. Only sets the end time
     * if the currently playing Playback is neither Loop nor Playlist, does nothing otherwise.
     * Crashes if the Playback hasn't been started yet.
     * @param millis offset from audio duration.
     */
    public void setEndTime(int millis) {
        Bundle bundle = new Bundle();
        bundle.putInt(MediaAction.EndTime, millis);
        MediaControllerCompat.getMediaController(this).getTransportControls().sendCustomAction(MediaAction.SetEndTime, bundle);
    }

    /**
     * Gets the start time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    public int getStartTime() {
        return (int) getMetadata().getLong(MetadataKey.StartPos);
    }

    /**
     * Gets the end time of the currently playing song. Crashes if the Playback hasn't been started yet.
     */
    public int getEndTime() {
        return (int) getMetadata().getLong(MetadataKey.EndPos);
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
        return MediaControllerCompat.getMediaController(this).getPlaybackState();
    }

    /**
     * @return metadata which was set in the MediaPlaybackService
     */
    public MediaMetadataCompat getMetadata() {
        return MediaControllerCompat.getMediaController(this).getMetadata();
    }

    /**
     * Connection to the MediaPlaybackService has been established.
     */
    protected void onConnected() {}


    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

            // Create a MediaControllerCompat
            MediaControllerCompat mediaController =
                    new MediaControllerCompat(MediaControllerActivity.this, token);

            // Save the controller
            MediaControllerCompat.setMediaController(MediaControllerActivity.this, mediaController);

            // Finish building the UI
            MediaControllerActivity.this.onConnected();

            // Register a Callback to stay in sync
            MediaControllerCompat.getMediaController(MediaControllerActivity.this).registerCallback(mControllerCallback);
            Log.d("Mucify", "MediaController connection established");
        }

        @Override
        public void onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
            Log.d("Mucify", "MediaController connection suspended");
        }

        @Override
        public void onConnectionFailed() {
            // The Service has refused our connection
            Log.d("Mucify", "MediaController connection failed");
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("Mucify", "MediaController metadata changed");

            String newTitle = metadata.getString(MetadataKey.Title);
            String newArtist = metadata.getString(MetadataKey.Artist);

            if(mPreviousMetadata == null || (newTitle != null && !newTitle.equals(mPreviousMetadata.getString(MetadataKey.Title))))
                for(Callback c : mCallbacks)
                    c.onTitleChanged(newTitle);

            if(mPreviousMetadata == null || (newArtist != null && !newArtist.equals(mPreviousMetadata.getString(MetadataKey.Artist))))
                for(Callback c : mCallbacks)
                    c.onArtistChanged(newArtist);

            mPreviousMetadata = metadata;
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("Mucify", "MediaController playback state changed" + state);

            if(mPreviousPlaybackState == null || state.getState() != mPreviousPlaybackState.getState()) {
                if(state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                    for(Callback c : mCallbacks)
                        c.onPause();
                }
                else if (mPreviousPlaybackState == null || state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    for(Callback c : mCallbacks)
                        c.onStart();
                }
            }

            if(mPreviousPlaybackState == null || state.getPosition() != mPreviousPlaybackState.getPosition()) {
                for(Callback c : mCallbacks)
                    c.onSeekTo((int) state.getPosition());
            }

            mPreviousPlaybackState = state;
        }

        @Override
        public void onSessionDestroyed() {
            Log.d("Mucify", "MediaController session destroyed");
            mMediaBrowser.disconnect();
            // maybe schedule a reconnection using a new MediaBrowser instance
        }
    }


    public abstract static class Callback {
        public void onStart() {}
        public void onPause() {}
        public void onTitleChanged(String title) {}
        public void onArtistChanged(String artist) {}
        public void onSeekTo(int millis) {}
    }

}
