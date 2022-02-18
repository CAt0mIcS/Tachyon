package com.de.mucify.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.MultiAudioPlayController;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Playlist;
import com.de.mucify.service.MediaSessionService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class MultiAudioPlayActivity extends AppCompatActivity {
    private Intent mSongPlayForegroundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_play_activity);
        MucifyApplication.setCurrentActivity(this);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        mSongPlayForegroundIntent = new Intent(this, MediaSessionService.class);

        btmNav.setSelectedItemId(R.id.playlists);
        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    Intent i = new Intent(MultiAudioPlayActivity.this, SingleAudioActivity.class);
                    i.putExtra("NavItemID", item.getItemId());
                    startActivity(i);
                    finish();
                    break;
                case R.id.playlists:
                    i = new Intent(MultiAudioPlayActivity.this, MultiAudioActivity.class);
                    startActivity(i);
                    finish();
                    break;
                case R.id.settings:
                    i = new Intent(MultiAudioPlayActivity.this, SettingsActivity.class);
                    startActivity(i);
                    finish();
                    break;
            }

            return true;
        });

        if(!getIntent().getBooleanExtra("PreserveAudio", false)) {
            AudioController.get().setPlaylist(new Playlist(this, new File(getIntent().getStringExtra("AudioFilePath"))));

            AudioController.get().addOnSongUnpausedListener(song -> {
                startService(mSongPlayForegroundIntent);
            }, AudioController.INDEX_DONT_CARE);

            stopService(mSongPlayForegroundIntent);
            startService(mSongPlayForegroundIntent);

            AudioController.get().startSong();
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        new MultiAudioPlayController(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MucifyApplication.activityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MucifyApplication.activityPaused(this);
    }
}
