package com.de.mucify.ui;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.adapter.AdapterEventListener;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.adapter.ViewHolderLoop;
import com.de.mucify.adapter.ViewHolderPlaylist;
import com.de.mucify.adapter.ViewHolderSong;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ActivityLibrary extends MediaControllerActivity implements AdapterEventListener {
    ArrayList<Playback> mHistory = new ArrayList<>();
    private boolean mRestartMinimizedPlayer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        Toolbar toolbar = (Toolbar)findViewById(R.id.my_toolbar);
        toolbar.inflateMenu(R.menu.toolbar_default);
        toolbar.setTitle(getString(R.string.library));

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.btmNavLibrary);


        findViewById(R.id.relLayoutSongs).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Song");
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutLoops).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Loop");
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutPlaylists).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Playlist");
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mRestartMinimizedPlayer = true;
    }

    @Override
    public void onConnected() {
        if(mRestartMinimizedPlayer) {
            Song current = getCurrentSong();
            if(current != null) {
                FragmentMinimizedPlayer fragmentMinimizedPlayer = new FragmentMinimizedPlayer(current, this);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                        .commit();
            }
        }
        mRestartMinimizedPlayer = false;

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        mHistory.addAll(MediaPlaybackService.Media.AvailableSongs);
        mHistory.addAll(MediaPlaybackService.Media.AvailableLoops);
        mHistory.addAll(MediaPlaybackService.Media.AvailablePlaylists);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mHistory);
        adapter.setListener(this);
        rvHistory.setAdapter(adapter);

        rvHistory.getAdapter().notifyDataSetChanged();
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
}