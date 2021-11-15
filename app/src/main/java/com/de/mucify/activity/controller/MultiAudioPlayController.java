package com.de.mucify.activity.controller;

import android.content.Intent;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.PlaylistAudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;

public class MultiAudioPlayController {
    private final MultiAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();

    private final TextView mLblName;
    private final SeekBar mSbProgress;
    private final TextView mLblProgress;

    private boolean mWasSongPaused = false;

    public MultiAudioPlayController(MultiAudioPlayActivity activity) {
        mActivity = activity;

        mLblName = mActivity.findViewById(R.id.pp_lblSongName);
        mSbProgress = mActivity.findViewById(R.id.pp_sbProgress);
        mLblProgress = mActivity.findViewById(R.id.pp_lblProgress);

        AudioController.SongFinishedListener listener = song -> {
            if(!PlaylistAudioController.get().isPlaylistNull()) {
                if(song.isLoop())
                    mLblName.setText(song.getLoopName());
                else
                    mLblName.setText(song.getTitle());
                mSbProgress.setMax(song.getDuration() / UserSettings.AudioUpdateInterval);
            }
        };
        listener.onFinished(AudioController.get().getSong());

        AudioController.get().addOnSongFinishedListener(listener, AudioController.INDEX_DONT_CARE);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!AudioController.get().isSongNull() && AudioController.get().isSongPlaying()) {
                    int mCurrentPosition = AudioController.get().getCurrentSongPosition() / UserSettings.AudioUpdateInterval;
                    mSbProgress.setProgress(mCurrentPosition);
                    mLblProgress.setText(Utils.millisecondsToReadableString(AudioController.get().getCurrentSongPosition()));
                }
                mHandler.postDelayed(this, UserSettings.AudioUpdateInterval);
            }
        });
        PlaylistAudioController.get().addOnPlaylistResetListener(song -> {
            if(MucifyApplication.isActivityVisible()) {
                Intent i = new Intent(mActivity, MultiAudioActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            }
        }, AudioController.INDEX_DONT_CARE);
        MucifyApplication.addOnActivityVisibilityChangedListener((act, becameVisible) -> {
            if(becameVisible && PlaylistAudioController.get().isPlaylistNull() && act instanceof MultiAudioPlayActivity) {
                Intent i = new Intent(mActivity, MultiAudioActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            }
        });
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { if(!mWasSongPaused) AudioController.get().unpauseSong(); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mWasSongPaused = !AudioController.get().isSongPlaying();
                AudioController.get().pauseSong();
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!AudioController.get().isSongNull() && fromUser){
                    AudioController.get().seekSongTo(progress * UserSettings.AudioUpdateInterval);
                    mLblProgress.setText(Utils.millisecondsToReadableString(AudioController.get().getCurrentSongPosition()));
                }
            }
        });
    }
}
