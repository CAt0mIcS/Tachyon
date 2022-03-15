package com.de.mucify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.de.mucify.R;
import com.de.mucify.player.Playback;

public class FragmentMinimizedPlayer extends Fragment {

    private String mTitle;
    private String mArtist;
    private Playback mPlayback;
    private ImageButton mPlayPause;
    private MediaControllerActivity mMediaController;

    private PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    public FragmentMinimizedPlayer() {
        super();
    }

    public FragmentMinimizedPlayer(Playback playback, MediaControllerActivity controller) {
        super(R.layout.fragment_minimized_player);
        mPlayback = playback;
        mPlayback.addCallback(mPlaybackCallback);
        mTitle = playback.getTitle();
        mArtist = playback.getSubtitle();
        mMediaController = controller;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.txtTitle)).setText(mTitle);
        ((TextView)view.findViewById(R.id.txtArtist)).setText(mArtist);

        mPlayPause = view.findViewById(R.id.btnPlayPause);
        mPlayPause.setOnClickListener(v -> {
            if(!mPlayback.isCreated())
                mMediaController.play(mPlayback);
            else if(mMediaController.isPaused())
                mMediaController.unpause();
            else
                mMediaController.pause();
        });

        // Clicking on minimized player should open the large player
        view.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), ActivityPlayer.class);
            i.putExtra("MediaId", mPlayback.getMediaId());
            i.putExtra("IsPlaying", true);
            startActivity(i);
        });

        if(!mPlayback.isCreated() || mPlayback.isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayback.removeCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Playback.Callback {
        @Override
        public void onStart() {
            if(mPlayPause != null)
                mPlayPause.setImageResource(R.drawable.pause);
        }

        @Override
        public void onPause() {
            if(mPlayPause != null)
                mPlayPause.setImageResource(R.drawable.play);
        }
    }
}
