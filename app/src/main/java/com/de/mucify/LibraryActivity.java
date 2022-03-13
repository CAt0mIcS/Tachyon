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

import com.de.mucify.player.MediaControllerActivity;
import com.de.mucify.service.MediaPlaybackService;

import java.security.Permission;

public class LibraryActivity extends MediaControllerActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onBuildTransportControls() {
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
    }
}