package com.de.mucify.ui;

import android.os.Bundle;

import com.de.mucify.MediaLibrary;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

public class ActivityPlaylistPlayer extends MediaControllerActivity {
    private int mPlaybackSeekPos = 0;
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCallback(mPlaybackCallback);
    }

    @Override
    public void onConnected() {
        if(!getIntent().getBooleanExtra("IsPlaying", false))
            play(getIntent().getStringExtra("MediaId"));

        mPlaybackSeekPos = getIntent().getIntExtra("SeekPos", 0);
    }

    private class PlaybackCallback extends Callback {

    }
}
