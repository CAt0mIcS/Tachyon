package com.de.mucify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.AudioType;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.adapter.AdapterEventListener;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.adapter.ViewHolderLoop;
import com.de.mucify.adapter.ViewHolderPlaylist;
import com.de.mucify.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ActivityLibrary extends MediaControllerActivity implements AdapterEventListener {
    ArrayList<Playback> mHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_default);
        toolbar.setTitle(getString(R.string.library));

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.btmNavLibrary);

        if(!checkPermission())
            requestPermission();


        findViewById(R.id.relLayoutSongs).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio(AudioType.Song);
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutLoops).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio(AudioType.Loop);
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutPlaylists).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio(AudioType.Playlist);
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected() {
        Song miniplayerSong = MediaPlaybackService.Media.getSong(UserData.LastPlayedPlayback);
        if(miniplayerSong != null) {
            FragmentMinimizedPlayer fragmentMinimizedPlayer =
                    new FragmentMinimizedPlayer(miniplayerSong, UserData.LastPlayedPlaybackPos, this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                    .commit();
        }


        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        mHistory.clear();
        mHistory.addAll(MediaPlaybackService.Media.AvailableSongs);
        mHistory.addAll(MediaPlaybackService.Media.AvailableLoops);
        mHistory.addAll(MediaPlaybackService.Media.AvailablePlaylists);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mHistory);
        adapter.setListener(this);
        rvHistory.setAdapter(adapter);
    }

    @Override
    public void onClick(ViewHolderSong holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    @Override
    public void onClick(ViewHolderLoop holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    @Override
    public void onClick(ViewHolderPlaylist holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    private void startAudio(Playback playback) {
        play(playback);

        FragmentMinimizedPlayer fragmentMinimizedPlayer = new FragmentMinimizedPlayer(playback, this);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                .commit();
    }


    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2296);
        }
    }
}