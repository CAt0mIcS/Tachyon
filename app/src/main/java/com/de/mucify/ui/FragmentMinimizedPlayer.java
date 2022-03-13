package com.de.mucify.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.de.mucify.R;
import com.de.mucify.player.Playback;

public class FragmentMinimizedPlayer extends Fragment implements Playback.Callback {

    private String mTitle;
    private String mArtist;
    private Playback mPlayback;
    private ImageButton mPlayPause;

    public FragmentMinimizedPlayer() {
        super();
    }

    public FragmentMinimizedPlayer(Playback playback) {
        super(R.layout.fragment_minimized_player);
        mPlayback = playback;
        mPlayback.setCallback(this);
        mTitle = playback.getTitle();
        mArtist = playback.getSubtitle();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.txtTitle)).setText(mTitle);
        ((TextView)view.findViewById(R.id.txtArtist)).setText(mArtist);

        mPlayPause = view.findViewById(R.id.btnPlayPause);
        mPlayPause.setOnClickListener(v -> {
            if(mPlayback.isPaused())
                mPlayback.unpause();
            else
                mPlayback.pause();
        });

        if(mPlayback.isPlaying())
            mPlayPause.setImageResource(R.drawable.pause);
    }


    @Override
    public void onPlayPause(boolean paused) {
        if(paused)
            mPlayPause.setImageResource(R.drawable.play);
        else
            mPlayPause.setImageResource(R.drawable.pause);
    }
}
