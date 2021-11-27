package com.de.mucify.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;
import com.de.mucify.service.SongPlayForegroundService;
import com.de.mucify.util.MediaLibrary;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class MultiAudioActivity extends AppCompatActivity {
    private Intent mSongPlayForegroundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_loop_playlist_select_activity);

        mSongPlayForegroundIntent = new Intent(this, SongPlayForegroundService.class);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.playlists);
        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    Intent i = new Intent(MultiAudioActivity.this, SingleAudioActivity.class);
                    i.putExtra("NavItemID", item.getItemId());
                    startActivity(i);
                    finish();
                    break;
            }

            return true;
        });

        if(!getIntent().getBooleanExtra("PreservePlaylist", false)) {
            MediaLibrary.loadAvailablePlaylists();
            Playlist playlist = MediaLibrary.AvailablePlaylists.get(0).create(this);
            AudioController.get().setPlaylist(playlist);
            AudioController.get().startSong();

            AudioController.get().addOnSongUnpausedListener(song -> {
                startService(mSongPlayForegroundIntent);
            }, AudioController.INDEX_DONT_CARE);

            stopService(mSongPlayForegroundIntent);
            startService(mSongPlayForegroundIntent);
        }

        // MY_TODO: Test if needs to be called in onResume?
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
