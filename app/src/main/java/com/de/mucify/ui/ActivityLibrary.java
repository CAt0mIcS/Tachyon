package com.de.mucify.ui;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class ActivityLibrary extends MediaControllerActivity {
    ArrayList<Playback> mPlaybacks = new ArrayList<>();

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

        mPlaybacks.addAll(MediaLibrary.AvailableSongs);
        mPlaybacks.addAll(MediaLibrary.AvailableLoops);
        mPlaybacks.addAll(MediaLibrary.AvailablePlaylists);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mPlaybacks);
        rvHistory.setAdapter(adapter);

        rvHistory.getAdapter().notifyDataSetChanged();


        findViewById(R.id.relLayoutSongs).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Song");
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutLoops).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Loop");
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });
        findViewById(R.id.relLayoutPlaylists).setOnClickListener(v -> {
            FragmentSelectAudio fragment = new FragmentSelectAudio("Playlist");
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, fragment)
                    .commit();
        });

        FragmentMinimizedPlayer fragmentMinimizedPlayer = new FragmentMinimizedPlayer("Title Hello", "Artist Hello");
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentMinimizedPlayer, fragmentMinimizedPlayer)
                .commit();
    }

    @Override
    public void onBuildTransportControls() {

    }
}