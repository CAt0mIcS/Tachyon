package com.de.mucify.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.common.AudioType;
import com.de.common.MediaLibrary;
import com.de.mucify.R;
import com.de.common.UserData;
import com.de.common.player.Playback;
import com.de.common.player.Playlist;
import com.de.mucify.ui.adapter.PlaybackListItemAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class ActivityLibrary extends MediaControllerActivity {
    private ArrayList<Playback> mHistory = new ArrayList<>();
    private UserDataCallback mUserDataCallback = new UserDataCallback();
    private RecyclerView mRvHistory;

    private final CountDownLatch mMediaLibraryLoaderLatch = new CountDownLatch(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_library);
        initializeToolbar();

        // MY_TODO: Find a good location to load MediaLibrary and UserData
        // MY_TODO: Sometimes loaded twice
        UserData.load(this);
        MediaLibrary.load(this);
        MediaLibrary.loadSongs(this, mMediaLibraryLoaderLatch::countDown);
        MediaLibrary.loadLoopsAndPlaylists(this, mMediaLibraryLoaderLatch::countDown);

        mRvHistory = findViewById(R.id.rvHistory);

        // MY_TEMPORARY: Set dark theme just to make it look better in the emulator
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.btmNavLibrary);
        btmNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.btmNavSearch) {
                Intent i = new Intent(ActivityLibrary.this, ActivitySearch.class);
                startActivity(i);
            }
            return true;
        });

        // MY_TEMPORARY
        if (!checkPermission())
            requestPermission();
        requestPermissions();

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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected() {
        try {
            mMediaLibraryLoaderLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        mRvHistory.setLayoutManager(new LinearLayoutManager(this));
        mHistory.clear();

        Playback miniplayerPlayback;
        if (UserData.getPlaybackInfoSize() == 0)
            return;
        miniplayerPlayback = MediaLibrary.getSong(UserData.getPlaybackInfo(UserData.getPlaybackInfoSize() - 1).PlaybackPath);

        if (miniplayerPlayback == null) {
            miniplayerPlayback = MediaLibrary.getPlaylist(UserData.getPlaybackInfo(UserData.getPlaybackInfoSize() - 1).PlaybackPath);
            if (miniplayerPlayback != null && UserData.getPlaybackInfo(UserData.getPlaybackInfoSize() - 1).LastPlayedPlaybackInPlaylist != null)
                ((Playlist) miniplayerPlayback).setCurrentSong(MediaLibrary.getSong(UserData.getPlaybackInfo(UserData.getPlaybackInfoSize() - 1).LastPlayedPlaybackInPlaylist));
        }

        if (miniplayerPlayback != null) {
            startMinimizedPlayer(miniplayerPlayback);
        }

        UserData.addCallback(mUserDataCallback);
        PlaybackListItemAdapter adapter = new PlaybackListItemAdapter(this, mHistory);
        adapter.setListener(new AdapterEventListener());
        mRvHistory.setAdapter(adapter);
        mUserDataCallback.onPlaybackInfoChanged();
    }

    @Override
    public void onDisconnected() {
        UserData.removeCallback(mUserDataCallback);
    }


    private class AdapterEventListener extends com.de.mucify.ui.adapter.AdapterEventListener {
        /**
         * Called whenever we click on a RecyclerView item. Starts playing the clicked audio
         */
        @Override
        public void onClick(RecyclerView.ViewHolder holder, int viewType) {
            startAudio(mHistory.get(holder.getAdapterPosition()));
        }
    }


    /**
     * Starts the audio and calls startMinimizedPlayer to start the FragmentMinimizedPlayer.
     */
    private void startAudio(Playback playback) {
        setMediaId(playback.getMediaId());
        play();
        startMinimizedPlayer(playback);
    }

    /**
     * Sets the data that the FragmentMinimizedPlayer needs and adds it to the fragment manager
     */
    private void startMinimizedPlayer(Playback playback) {
        if (!isCreated())
            setMediaId(playback.getMediaId());

        Bundle bundle = new Bundle();
        bundle.putString("MediaId", playback.getMediaId());

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
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
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


    private boolean mPermissionResultHere = false;

    /**
     * Requests all necessary permissions and returns once they're granted
     * MY_TODO: Already load certain things while user still accepts permission
     * MY_TODO: Dialog explaining why we need permission
     */
    private void requestPermissions() {

        ActivityResultLauncher<String> mRequestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    mPermissionResultHere = true;
                });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            mRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            while (!mPermissionResultHere) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Used to update the RecyclerView listing the history. MY_TODO: Make more efficient by
     * adding information about which items changed and not using Adapter.notifyDataSetChanged()
     */
    private class UserDataCallback extends UserData.Callback {
        @Override
        public void onPlaybackInfoChanged() {
            mHistory.clear();
            for (int i = UserData.getPlaybackInfoSize() - 1; i >= 0; --i) {
                if (UserData.getPlaybackInfo(i).isPlaylist()) {
                    Playback playback = MediaLibrary.getPlaybackFromPath(UserData.getPlaybackInfo(i).LastPlayedPlaybackInPlaylist);
                    if (playback != null)
                        mHistory.add(playback);
                } else {
                    Playback playback = MediaLibrary.getPlaybackFromPath(UserData.getPlaybackInfo(i).PlaybackPath);
                    if (playback != null)
                        mHistory.add(playback);
                }
            }
            mRvHistory.getAdapter().notifyDataSetChanged();
        }
    }
}