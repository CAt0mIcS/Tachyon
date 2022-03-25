package com.de.mucify.ui;

import android.os.Bundle;

public class ActivityPlaylistPlayer extends MediaControllerActivity {
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addCallback(mPlaybackCallback);

        onConnected();
    }

    public void onConnected() {
        if (!getIntent().getBooleanExtra("IsPlaying", false))
            play(getIntent().getStringExtra("MediaId"));
    }

    private class PlaybackCallback extends Callback {

    }
}
