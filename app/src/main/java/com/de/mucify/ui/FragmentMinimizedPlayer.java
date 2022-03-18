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

    private String mMediaId;
    private String mTitle;
    private String mArtist;
    private ImageButton mPlayPause;
    private TextView mTxtTitle;
    private TextView mTxtArtist;

    private MediaControllerActivity mMediaController;

    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();
    private int mPlaybackSeekPos;

    public FragmentMinimizedPlayer() {
        super();
    }

    public FragmentMinimizedPlayer(String mediaId, String title, String artist, int playbackSeekPos, MediaControllerActivity controller) {
        super(R.layout.fragment_minimized_player);
        mMediaController = controller;
        mPlaybackSeekPos = playbackSeekPos;
        mMediaId = mediaId;
        mTitle = title;
        mArtist = artist;

        mMediaController.addCallback(mPlaybackCallback);
    }

    public FragmentMinimizedPlayer(String mediaId, String title, String artist, MediaControllerActivity controller) {
        this(mediaId, title, artist, 0, controller);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTxtTitle = view.findViewById(R.id.txtTitle);
        mTxtArtist = view.findViewById(R.id.txtArtist);
        mTxtTitle.setText(mTitle);
        mTxtArtist.setText(mArtist);

        mPlayPause = view.findViewById(R.id.btnPlayPause);
        mPlayPause.setOnClickListener(v -> {
            if(!mMediaController.isCreated()) {
                mMediaController.play(mMediaId);
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
            i.putExtra("MediaId", mMediaId);

            // Player only needs title and artist if the Playback hasn't been started yet
            if(!mMediaController.isCreated()) {
                i.putExtra("Title", mTxtTitle.getText().toString());
                i.putExtra("Subtitle", mTxtArtist.getText().toString());
            }

            i.putExtra("IsPlaying", true);
            if(mPlaybackSeekPos != 0)
                i.putExtra("SeekPos", mPlaybackSeekPos);
            startActivity(i);
        });

        if(!mMediaController.isCreated() || mMediaController.isPaused())
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

        @Override
        public void onTitleChanged(String title) {
            mTxtTitle.setText(title);
        }

        @Override
        public void onArtistChanged(String artist) {
            mTxtArtist.setText(artist);
        }
    }
}
