package com.de.mucify.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.SingleAudioPlayController;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.service.MediaSessionService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SingleAudioPlayActivity extends AppCompatActivity {
    private Intent mSongPlayForegroundIntent;

    private static SingleAudioPlayActivity sInstance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;

        setContentView(R.layout.song_loop_play_activity);

        mSongPlayForegroundIntent = new Intent(this, MediaSessionService.class);
        BottomNavigationView btmNav = findViewById(R.id.btmNav);

        // When switching to this activity, we need to know if we want the Song
        // or Loop menu item selected, default will be Song item.
        // Doing this before setting the listener, otherwise we would immediately switch back to
        // the song/loop/playlist select activity
        int navItemID = getIntent().getIntExtra("NavItemID", R.id.songs);
        btmNav.setSelectedItemId(navItemID);

        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    Intent i = new Intent(SingleAudioPlayActivity.this, SingleAudioActivity.class);
                    i.putExtra("NavItemID", item.getItemId());
                    startActivity(i);
                    finish();
                    break;
                case R.id.playlists:
                    i = new Intent(SingleAudioPlayActivity.this, MultiAudioActivity.class);
                    startActivity(i);
                    finish();
                    break;
            }

            return true;
        });

        if(!getIntent().getBooleanExtra("PreserveSong", false)) {
            AudioController.get().setSong(new Song(this, new File(getIntent().getStringExtra("AudioFilePath"))));

            AudioController.get().addOnSongUnpausedListener(song -> {
                startService(mSongPlayForegroundIntent);
            }, AudioController.INDEX_DONT_CARE);

            stopService(mSongPlayForegroundIntent);
            startService(mSongPlayForegroundIntent);

            AudioController.get().startSong();
        }

        // MY_TODO: Test if needs to be called in onResume?
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        new SingleAudioPlayController(this);
    }

    public int getNavItemID() {
        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        return btmNav.getSelectedItemId();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MucifyApplication.activityResumed(this);
        sInstance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MucifyApplication.activityPaused(this);
    }

    public static SingleAudioPlayActivity get() {
        return sInstance;
    }
}
