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
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.PermissionManager;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;


public abstract class MediaControllerActivity extends AppCompatActivity {
    private MediaBrowserCompat mMediaBrowser;
    private final MediaControllerCallback mControllerCallback = new MediaControllerCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MY_TODO: Better waiting and figure out what to do if permission not granted
        PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        // Not using startForegroundService because the service only has 5 seconds to call Service.startForeground
        // which doesn't happen until we actually start playing audio
        startService(new Intent(this, MediaPlaybackService.class));

        mMediaBrowser = new MediaBrowserCompat(MediaControllerActivity.this,
                new ComponentName(MediaControllerActivity.this, MediaPlaybackService.class),
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
        if (MediaControllerCompat.getMediaController(MediaControllerActivity.this) != null) {
            MediaControllerCompat.getMediaController(MediaControllerActivity.this).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
        Log.d("Mucify", "Started disconnecting from MediaController");
    }

    public void unpause() {
        MediaControllerCompat.getMediaController(this).getTransportControls().play();
    }

    public void pause() {
        MediaControllerCompat.getMediaController(this).getTransportControls().pause();
    }

    public void seekTo(int millis) {
        MediaControllerCompat.getMediaController(this).getTransportControls().seekTo(millis);
    }

    public void play(Playback playback) {
        MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(playback.getMediaId(), null);
    }


    public void play(String mediaId) {
        MediaControllerCompat.getMediaController(this).getTransportControls().playFromMediaId(mediaId, null);
    }


    public boolean isPlaying() {
        return MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
    }

    public boolean isPaused() {
        return MediaControllerCompat.getMediaController(this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED;
    }

    private void buildTransportControls()
    {
        onBuildTransportControls();
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MediaControllerActivity.this);
        // Register a Callback to stay in sync
        mediaController.registerCallback(mControllerCallback);
    }

    protected void onBuildTransportControls() {}
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
            buildTransportControls();
            MediaControllerActivity.this.onConnected();
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
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("Mucify", "MediaController playback state changed" + state);
        }

        @Override
        public void onSessionDestroyed() {
            Log.d("Mucify", "MediaController session destroyed");
            mMediaBrowser.disconnect();
            // maybe schedule a reconnection using a new MediaBrowser instance
        }
    }
}
