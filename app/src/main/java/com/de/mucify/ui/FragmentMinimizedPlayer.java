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
import com.de.mucify.Util;
import com.de.mucify.player.Playback;

public class FragmentMinimizedPlayer extends Fragment {

    private String mTitle;
    private String mArtist;
    private Playback mPlayback;
    private ImageButton mPlayPause;
    private MediaControllerActivity mMediaController;

    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();
    private int mPlaybackSeekPos;

    public FragmentMinimizedPlayer() {
        super();
    }

    public FragmentMinimizedPlayer(Playback playback, int playbackSeekPos, MediaControllerActivity controller) {
        super(R.layout.fragment_minimized_player);
        mPlayback = playback;
        mTitle = playback.getTitle();
        mArtist = playback.getSubtitle();
        mMediaController = controller;
        mPlaybackSeekPos = playbackSeekPos;

        mMediaController.addCallback(mPlaybackCallback);
    }

    public FragmentMinimizedPlayer(Playback playback, MediaControllerActivity controller) {
        this(playback, 0, controller);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.txtTitle)).setText(mTitle);
        ((TextView)view.findViewById(R.id.txtArtist)).setText(mArtist);

        mPlayPause = view.findViewById(R.id.btnPlayPause);
        mPlayPause.setOnClickListener(v -> {
            if(!mPlayback.isCreated()) {
                mMediaController.play(mPlayback);
                if(mPlaybackSeekPos != 0) {
                    mMediaController.seekTo(mPlaybackSeekPos);
                }

            }
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
            if(mPlaybackSeekPos != 0)
                i.putExtra("SeekPos", mPlaybackSeekPos);
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
        mMediaController.removeCallback(mPlaybackCallback);
    }


    private class PlaybackCallback extends MediaControllerActivity.Callback {
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
