package com.mucify;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mucify.ui.fragments.LoopSelectFragment;
import com.mucify.ui.fragments.PlaylistSelectFragment;
import com.mucify.ui.fragments.SettingsFragment;
import com.mucify.ui.fragments.SongSelectFragment;
import com.mucify.ui.internal.MediaButtonIntentReceiver;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.main_activity);

        initialize();
        loadFragment(new SongSelectFragment());
    }

    private void initialize() {
        try {
            PermissionManager.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            PermissionManager.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            PermissionManager.requestPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Globals.load(this);
        } catch(IOException e) {
            Utils.messageBox(this, "Data read failed", e.getMessage());
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.songs:
                    return loadFragment(new SongSelectFragment());
                case R.id.loops:
                    return loadFragment(new LoopSelectFragment());
                case R.id.playlists:
                    return loadFragment(new PlaylistSelectFragment());
                case R.id.settings:
                    return loadFragment(new SettingsFragment());
            }
            return false;
        });

        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        MediaButtonIntentReceiver r = new MediaButtonIntentReceiver();
        filter.setPriority(1000);
        registerReceiver(r, filter);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
