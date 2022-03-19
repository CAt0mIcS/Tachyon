package com.de.mucify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.AudioType;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.adapter.AdapterEventListener;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.adapter.ViewHolderLoop;
import com.de.mucify.adapter.ViewHolderPlaylist;
import com.de.mucify.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ActivityLibrary extends MediaControllerActivity implements AdapterEventListener {
    ArrayList<Playback> mHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        initializeToolbar();

        // MY_TEMPORARY: Set dark theme just to make it look better in the emulator
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.btmNavLibrary);

        // MY_TEMPORARY
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

        CastContext castContext = CastContext.getSharedInstance(this);
    }

    @Override
    public void onConnected() {
        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        mHistory.clear();

        Playback miniplayerPlayback;
        if(UserData.PlaybackInfos.size() == 0)
            return;
        miniplayerPlayback = MediaPlaybackService.Media.getSong(UserData.PlaybackInfos.get(UserData.PlaybackInfos.size() - 1).PlaybackPath);

        if(miniplayerPlayback == null) {
            miniplayerPlayback = MediaPlaybackService.Media.getPlaylist(UserData.PlaybackInfos.get(UserData.PlaybackInfos.size() - 1).PlaybackPath);
            if(miniplayerPlayback != null && UserData.PlaybackInfos.get(UserData.PlaybackInfos.size() - 1).LastPlayedPlaybackInPlaylist != null)
                ((Playlist)miniplayerPlayback).setCurrentSong(MediaPlaybackService.Media.getSong(UserData.PlaybackInfos.get(UserData.PlaybackInfos.size() - 1).LastPlayedPlaybackInPlaylist));
        }

        if(miniplayerPlayback != null) {
            startMinimizedPlayer(miniplayerPlayback);
        }

        for(int i = UserData.PlaybackInfos.size() - 1; i >= 0; --i) {
            if(UserData.PlaybackInfos.get(i).isPlaylist()) {
                mHistory.add(MediaLibrary.getPlaybackFromPath(UserData.PlaybackInfos.get(i).LastPlayedPlaybackInPlaylist));
            }
            else {
                mHistory.add(MediaLibrary.getPlaybackFromPath(UserData.PlaybackInfos.get(i).PlaybackPath));
            }
        }

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mHistory);
        adapter.setListener(this);
        rvHistory.setAdapter(adapter);
    }

    /**
     * Called whenever we click on a RecyclerView Song item. Starts playing the clicked audio
     */
    @Override
    public void onClick(ViewHolderSong holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    /**
     * Called whenever we click on a RecyclerView Loop item. Starts playing the clicked audio
     */
    @Override
    public void onClick(ViewHolderLoop holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    /**
     * Called whenever we click on a RecyclerView Playlist item. Starts playing the clicked audio
     */
    @Override
    public void onClick(ViewHolderPlaylist holder) {
        startAudio(mHistory.get(holder.getAdapterPosition()));
    }

    /**
     * Starts the audio and calls startMinimizedPlayer to start the FragmentMinimizedPlayer.
     */
    private void startAudio(Playback playback) {
        play(playback.getMediaId());
        startMinimizedPlayer(playback);
    }

    /**
     * Sets the data that the FragmentMinimizedPlayer needs and adds it to the fragment manager
     */
    private void startMinimizedPlayer(Playback playback) {
        Bundle bundle = new Bundle();
        bundle.putString("MediaId", playback.getMediaId());
        bundle.putString("Title", playback.getTitle());
        bundle.putString("Subtitle", playback.getSubtitle());

        FragmentMinimizedPlayer fragmentMinimizedPlayer = new FragmentMinimizedPlayer();
        fragmentMinimizedPlayer.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                .commit();
    }


    /**
     * MY_TEMPORARY: Checks if we have all permissions, copied here to quickly write every unhandled
     * exception to a external file
     */
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Requests permission to manage all external files. This is used to log unhandled exceptions
     * to an external file and is MY_TEMPORARY.
     */
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