package com.de.mucify.ui;

import android.os.Bundle;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ActivityLibrary extends MediaControllerActivity implements AdapterEventListener {
    ArrayList<Playback> mHistory = new ArrayList<>();

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

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        MediaLibrary.loadAvailableSongs();
        MediaLibrary.loadAvailableLoops();
        MediaLibrary.loadAvailablePlaylists();

        mHistory.addAll(MediaLibrary.AvailableSongs);
        mHistory.addAll(MediaLibrary.AvailableLoops);
        mHistory.addAll(MediaLibrary.AvailablePlaylists);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mHistory);
        adapter.setListener(this);
        rvHistory.setAdapter(adapter);

        rvHistory.getAdapter().notifyDataSetChanged();


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
    public void onBuildTransportControls() {

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

        FragmentMinimizedPlayer fragmentMinimizedPlayer = new FragmentMinimizedPlayer(playback);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                .commit();
    }
}