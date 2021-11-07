package com.de.mucify.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.R;
import com.de.mucify.activity.controller.SingleAudioPlayController;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.service.SongPlayForegroundService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;

public class SingleAudioPlayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_loop_play_activity);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);
        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {

            }

            return true;
        });

        AudioController.get().setSong(new Song(this, new File(getIntent().getStringExtra("SongFilePath"))));
        AudioController.get().startSong();
        AudioController.get().addOnSongUnpausedListener(song -> {
            Intent songPlayForegroundIntent = new Intent(this, SongPlayForegroundService.class);
            startService(songPlayForegroundIntent);
        }, AudioController.INDEX_DONT_CARE);

        Intent songPlayForegroundIntent = new Intent(this, SongPlayForegroundService.class);
        startService(songPlayForegroundIntent);

        new SingleAudioPlayController(this);
    }
}
