package com.de.mucify;

import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.adapter.PlayableListItemAdapter;
import com.de.mucify.player.MediaControllerActivity;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LibraryActivity extends MediaControllerActivity {
    ArrayList<Playback> mPlaybacks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setSelectedItemId(R.id.btmNavLibrary);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        MediaLibrary.loadAvailableSongs();
        MediaLibrary.loadAvailableLoops();
        MediaLibrary.loadAvailablePlaylists();

        for(Song s : MediaLibrary.AvailableSongs)
            mPlaybacks.add(s);
        for(Song s : MediaLibrary.AvailableLoops)
            mPlaybacks.add(s);
        for(Playlist s : MediaLibrary.AvailablePlaylists)
            mPlaybacks.add(s);

        PlayableListItemAdapter adapter = new PlayableListItemAdapter(this, mPlaybacks);
        rvHistory.setAdapter(adapter);

        rvHistory.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onBuildTransportControls() {
        // Grab the view for the play/pause button
        RelativeLayout playPause = findViewById(R.id.relLayoutSongs);

        // Attach a listener to the button
        playPause.setOnClickListener(v -> {
            int pbState = MediaControllerCompat.getMediaController(LibraryActivity.this).getPlaybackState().getState();
            if (pbState == PlaybackStateCompat.STATE_PLAYING)
                MediaControllerCompat.getMediaController(LibraryActivity.this).getTransportControls().pause();
            else
                MediaControllerCompat.getMediaController(LibraryActivity.this).getTransportControls().play();
        });
    }
}