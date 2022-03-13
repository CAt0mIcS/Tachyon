package com.de.mucify.player;

import android.Manifest;
import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.PermissionManager;
import com.de.mucify.R;
import com.de.mucify.service.MediaPlaybackService;

public abstract class MediaControllerActivity extends AppCompatActivity {
    private MediaBrowserCompat mMediaBrowser;
    private final MediaControllerCallback mControllerCallback = new MediaControllerCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // MY_TODO: Better waiting and figure out what to do if permission not granted
        PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        mMediaBrowser = new MediaBrowserCompat(MediaControllerActivity.this,
                new ComponentName(MediaControllerActivity.this, MediaPlaybackService.class),
                new ConnectionCallback(),
                null);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(MediaControllerActivity.this) != null) {
            MediaControllerCompat.getMediaController(MediaControllerActivity.this).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    private void buildTransportControls()
    {
        onBuildTransportControls();
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MediaControllerActivity.this);
        // Register a Callback to stay in sync
        mediaController.registerCallback(mControllerCallback);
    }

    public abstract void onBuildTransportControls();


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
        }

        @Override
        public void onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        @Override
        public void onConnectionFailed() {
            // The Service has refused our connection
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {}

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {}

        @Override
        public void onSessionDestroyed() {
            mMediaBrowser.disconnect();
            // maybe schedule a reconnection using a new MediaBrowser instance
        }
    }
}
