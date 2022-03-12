package com.de.mucify;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;

import com.de.mucify.service.MediaPlaybackService;

import java.security.Permission;

public class LibraryActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;
    private final MediaControllerCallback mControllerCallback = new MediaControllerCallback();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MY_TODO: Better waiting and figure out what to do if permission not granted
        PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        mMediaBrowser = new MediaBrowserCompat(LibraryActivity.this,
                new ComponentName(LibraryActivity.this, MediaPlaybackService.class),
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
        if (MediaControllerCompat.getMediaController(LibraryActivity.this) != null) {
            MediaControllerCompat.getMediaController(LibraryActivity.this).unregisterCallback(mControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    void buildTransportControls()
    {
        // Grab the view for the play/pause button
        Button playPause = findViewById(R.id.btn_play_pause);

        // Attach a listener to the button
        playPause.setOnClickListener(v -> {
            int pbState = MediaControllerCompat.getMediaController(LibraryActivity.this).getPlaybackState().getState();
            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                MediaControllerCompat.getMediaController(LibraryActivity.this).getTransportControls().pause();
            else
                MediaControllerCompat.getMediaController(LibraryActivity.this).getTransportControls().play();
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(LibraryActivity.this);

        // Display the initial state
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // Register a Callback to stay in sync
        mediaController.registerCallback(mControllerCallback);
    }


    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

            // Create a MediaControllerCompat
            MediaControllerCompat mediaController =
                    new MediaControllerCompat(LibraryActivity.this, token);

            // Save the controller
            MediaControllerCompat.setMediaController(LibraryActivity.this, mediaController);

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