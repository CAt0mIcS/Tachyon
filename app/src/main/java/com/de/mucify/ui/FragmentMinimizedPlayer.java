package com.de.mucify.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.de.common.MediaLibrary;
import com.de.mucify.R;
import com.de.common.UserData;
import com.de.common.Util;

public class FragmentMinimizedPlayer extends Fragment {

    private ImageButton mPlayPause;
    private TextView mTxtTitle;
    private TextView mTxtArtist;

    private MediaControllerActivity mMediaController;
    private int mPlaybackSeekPos = 0;

    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    public FragmentMinimizedPlayer() {
        super(R.layout.fragment_minimized_player);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
        mMediaController = (MediaControllerActivity) getActivity();
        mPlaybackSeekPos = UserData.getPlaybackInfo(UserData.getPlaybackInfoSize() - 1).PlaybackPos;

        if (getArguments() == null)
            throw new UnsupportedOperationException("Argument for FragmentMinimizedPlayer must be set");
        if (mMediaController == null)
            throw new UnsupportedOperationException("FragmentMinimizedPlayer must've been started with a MediaBrowserController");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMediaController.addCallback(mPlaybackCallback);

        mTxtTitle = view.findViewById(R.id.txtTitle);
        mTxtArtist = view.findViewById(R.id.txtArtist);

        mPlayPause = view.findViewById(R.id.btnPlayPause);
        mPlayPause.setOnClickListener(v -> {
            if (mMediaController.isPaused())
                mMediaController.unpause();
            else
                mMediaController.pause();
        });

        // Clicking on minimized player should open the large player
        view.setOnClickListener(v -> {
            Intent i;
            if (MediaLibrary.isPlaylistMediaId(getArguments().getString("MediaId")))
                i = new Intent(getActivity(), ActivityPlaylistPlayer.class);
            else
                i = new Intent(getActivity(), ActivityPlayer.class);

            // Don't automatically start playing if the minimized player was clicked
            i.putExtra("StartPlaying", false);

            if (mPlaybackSeekPos != 0)
                i.putExtra("SeekPos", mPlaybackSeekPos);
            startActivity(i);
        });

        if (mMediaController.isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onPlay();
        mPlaybackCallback.onMediaIdChanged(getArguments().getString("MediaId"));
    }

    private class PlaybackCallback extends MediaControllerActivity.Callback {
        @Override
        public void onPlay() {
            if (mPlayPause != null)
                mPlayPause.setImageResource(R.drawable.pause);

            if (mPlaybackSeekPos != 0) {
                mMediaController.seekTo(mPlaybackSeekPos);
                mPlaybackSeekPos = 0;
            }
        }

        @Override
        public void onPause() {
            if (mPlayPause != null)
                mPlayPause.setImageResource(R.drawable.play);
        }

        @Override
        public void onCastConnected() {
            mMediaController.play();
        }

        @Override
        public void onCastDisconnected() {

        }

        @Override
        public void onMediaIdChanged(String mediaId) {
            getArguments().putString("MediaId", mediaId);
            mTxtTitle.setText(mMediaController.getSongTitle());
            mTxtArtist.setText(mMediaController.getSongArtist());
        }
    }
}
