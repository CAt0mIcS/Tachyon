package com.de.mucify.ui;

import android.os.Bundle;

import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

public class ActivityPlaylistPlayer extends MediaControllerActivity {
    private int mPlaybackSeekPos = 0;
    private Playlist mPlaylist;
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBuildTransportControls() {
        if(!getIntent().getBooleanExtra("IsPlaying", false))
            play(getIntent().getStringExtra("MediaId"));

        mPlaybackSeekPos = getIntent().getIntExtra("SeekPos", 0);

        mPlaylist = (Playlist)Util.getPlaybackFromMediaId(getIntent().getStringExtra("MediaId"));
        mPlaylist.addCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Playback.Callback {

    }
}
