package com.de.mucify.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.R;
import com.de.mucify.services.SongPlayForegroundService;
import com.de.mucify.utils.PermissionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SingleAudioActivity extends AppCompatActivity {
    private static SingleAudioActivity sInstance = null;

    private Intent mSongPlayForegroundIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;

        try {
            PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.song_loop_playlist_select_activity);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);

        // If the playlist switches back to this activity, we need to know if we want the Song
        // or Loop menu item selected, default will be Song item
        int navItemID = getIntent().getIntExtra("NavItemID", R.id.songs);
        btmNav.setSelectedItemId(navItemID);

        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.playlists:
                    Intent i = new Intent(SingleAudioActivity.this, MultiAudioActivity.class);
                    startActivity(i);
                    finish();
                    break;
                case R.id.settings:
                    return false;
            }

            return true;
        });

        mSongPlayForegroundIntent = new Intent(this, SongPlayForegroundService.class);
        mSongPlayForegroundIntent.putExtra("SongFilePath", "/data/user/0/com.de.mucify/files/Match In The Rain - Alec Benjamin.mp3");
        startService(mSongPlayForegroundIntent);
    }

    public static SingleAudioActivity get() {
        return sInstance;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }
}
