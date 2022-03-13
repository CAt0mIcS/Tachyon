package com.de.mucify.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.de.mucify.R;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;

public class ActivityPlayer extends MediaControllerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_layout);
    }

    @Override
    public void onBuildTransportControls() {
        play(getIntent().getStringExtra("MediaId"));
        seekTo(getIntent().getIntExtra("MediaPos", 0));

        Playback playback = Util.getPlaybackFromMediaId(getIntent().getStringExtra("MediaId"));

        ((TextView)findViewById(R.id.txtTitle)).setText(playback.getTitle());
        ((TextView)findViewById(R.id.txtArtist)).setText(playback.getSubtitle());


    }
}
