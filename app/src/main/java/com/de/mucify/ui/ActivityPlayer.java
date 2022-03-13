package com.de.mucify.ui;

import android.os.Bundle;

import com.de.mucify.R;

public class ActivityPlayer extends MediaControllerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_layout);
    }

    @Override
    public void onBuildTransportControls() {
        play(getIntent().getStringExtra("MediaId"));
    }
}
