package com.de.mucify.activity.controller;

import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;

import java.io.IOException;


public class SingleAudioPlayController {
    private final SingleAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();
    private final SeekBar mProgressSeekBar;
    private final TextView mLblProgress;
    private final TextView mLblStartTime;
    private final TextView mLblEndTime;
    private final ImageButton mBtnPlayPause;

    private boolean mWasSongPaused = false;

    public SingleAudioPlayController(SingleAudioPlayActivity activity) {
        mActivity = activity;

        mProgressSeekBar = mActivity.findViewById(R.id.pa_sbProgress);
        SeekBar startTimeSeekBar = mActivity.findViewById(R.id.pa_sbStartTime);
        SeekBar endTimeSeekBar = mActivity.findViewById(R.id.pa_sbEndTime);
        mLblProgress = mActivity.findViewById(R.id.pa_lblProgress);
        mLblStartTime = mActivity.findViewById(R.id.pa_lblStartTime);
        mLblEndTime = mActivity.findViewById(R.id.pa_lblEndTime);
        mBtnPlayPause = mActivity.findViewById(R.id.pa_btnPause);


        int duration = AudioController.get().getSongDuration() / UserSettings.AudioUpdateInterval;
        mProgressSeekBar.setMax(duration);
        startTimeSeekBar.setMax(duration);
        endTimeSeekBar.setMax(duration);

        ((TextView)mActivity.findViewById(R.id.pa_lblSongName)).setText(AudioController.get().getSongTitle() + " by " + AudioController.get().getSongArtist());

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!AudioController.get().isSongNull() && AudioController.get().isSongPlaying()) {
                    int mCurrentPosition = AudioController.get().getCurrentSongPosition() / UserSettings.AudioUpdateInterval;
                    mProgressSeekBar.setProgress(mCurrentPosition);
                    mLblProgress.setText(Utils.millisecondsToReadableString(AudioController.get().getCurrentSongPosition()));
                }
                mHandler.postDelayed(this, UserSettings.AudioUpdateInterval);
            }
        });

        mProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        startTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserSettings.AudioUpdateInterval;
                AudioController.get().setSongStartTime(startTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLblStartTime.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });
        endTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = seekBar.getProgress() * UserSettings.AudioUpdateInterval;
                AudioController.get().setSongEndTime(endTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLblEndTime.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });
        mBtnPlayPause.setOnClickListener(v -> {
            if(AudioController.get().isSongPlaying()) {
                AudioController.get().pauseSong();
                mBtnPlayPause.setImageResource(R.drawable.ic_black_play);
            }
            else {
                AudioController.get().unpauseSong();
                mBtnPlayPause.setImageResource(R.drawable.ic_black_pause);
            }
        });
        mActivity.findViewById(R.id.pa_btnSave).setOnClickListener(v -> {
            try {
                AudioController.get().saveAsLoop("LoopName");
                MediaLibrary.loadAvailableLoops();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Perform click to update image
        mBtnPlayPause.performClick();
        mBtnPlayPause.performClick();

        // Call listeners to set label text
        startTimeSeekBar.setProgress(1);
        startTimeSeekBar.setProgress(AudioController.get().getSongStartTime() != 0 ? AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval : 0);
        endTimeSeekBar.setProgress(AudioController.get().getSongEndTime() != 0 ? AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval : 0);
    }
}
