package com.de.mucify.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.SingleAudioPlayController;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.service.SongPlayForegroundService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SingleAudioPlayActivity extends AppCompatActivity {
    private Intent mSongPlayForegroundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_loop_play_activity);

        mSongPlayForegroundIntent = new Intent(this, SongPlayForegroundService.class);
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
            }

            return true;
        });

        AudioController.get().setSong(new Song(this, new File(getIntent().getStringExtra("AudioFilePath"))));
        AudioController.get().startSong();
        AudioController.get().addOnSongUnpausedListener(song -> {
            startService(mSongPlayForegroundIntent);
        }, AudioController.INDEX_DONT_CARE);

        stopService(mSongPlayForegroundIntent);
        startService(mSongPlayForegroundIntent);

        new SingleAudioPlayController(this);
    }

    public int getNavItemID() {
        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        return btmNav.getSelectedItemId();
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
