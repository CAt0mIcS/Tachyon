package com.de.mucify.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.R;
import com.de.mucify.activity.controller.AudioSelectController;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.PermissionManager;
import com.de.mucify.util.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SingleAudioActivity extends AppCompatActivity {
    private static SingleAudioActivity sInstance = null;

    public interface MenuItemChangedListener {
        void onItemChanged(MenuItem item);
    }
    public MenuItemChangedListener menuItemChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;

        Utils.enableDarkMode();

        try {
            PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.song_loop_playlist_select_activity);
        MediaLibrary.load(this);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);

        // If the playlist switches back to this activity, we need to know if we want the Song
        // or Loop menu item selected, default will be Song item
        int navItemID = getIntent().getIntExtra("NavItemID", R.id.songs);
        btmNav.setSelectedItemId(navItemID);

        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    if(menuItemChangedListener != null)
                        menuItemChangedListener.onItemChanged(item);
                    break;
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

        new AudioSelectController(this);
    }

    public static SingleAudioActivity get() { return sInstance; }
    public boolean isInSongTab() { return ((BottomNavigationView)findViewById(R.id.btmNav)).getSelectedItemId() == R.id.songs; }
    public boolean isInLoopTab() { return !isInSongTab(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }
}
