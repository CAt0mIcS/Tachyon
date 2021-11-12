package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;


public class SingleAudioPlayController {
    private final SingleAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();
    private final SeekBar mSbProgress;
    private final SeekBar mSbStartTime;
    private final SeekBar mSbEndTime;
    private final TextView mLblProgress;
    private final TextView mLblStartTime;
    private final TextView mLblEndTime;
    private final ImageButton mBtnPlayPause;

    private boolean mWasSongPaused = false;

    public SingleAudioPlayController(SingleAudioPlayActivity activity) {
        mActivity = activity;

        mSbProgress = mActivity.findViewById(R.id.pa_sbProgress);
        mSbStartTime = mActivity.findViewById(R.id.pa_sbStartTime);
        mSbEndTime = mActivity.findViewById(R.id.pa_sbEndTime);
        mLblProgress = mActivity.findViewById(R.id.pa_lblProgress);
        mLblStartTime = mActivity.findViewById(R.id.pa_lblStartTime);
        mLblEndTime = mActivity.findViewById(R.id.pa_lblEndTime);
        mBtnPlayPause = mActivity.findViewById(R.id.pa_btnPause);


        int duration = AudioController.get().getSongDuration() / UserSettings.AudioUpdateInterval;
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        ((TextView)mActivity.findViewById(R.id.pa_lblSongName)).setText(AudioController.get().getSongTitle() + " " + mActivity.getString(R.string.by) + " " + AudioController.get().getSongArtist());

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
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        mSbEndTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
            if(AudioController.get().isSongPlaying())
                AudioController.get().pauseSong();
            else
                AudioController.get().unpauseSong();
        });
        AudioController.get().addOnSongPausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_black_play), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongUnpausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_black_pause), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongResetListener(song -> {
            if(MucifyApplication.isActivityVisible()) {
                Intent i = new Intent(mActivity, SingleAudioActivity.class);
                i.putExtra("NavItemID", mActivity.getNavItemID());
                mActivity.startActivity(i);
                mActivity.finish();
            }
        }, AudioController.INDEX_DONT_CARE);
        MucifyApplication.addOnActivityVisibilityChangedListener((act, becameVisible) -> {
            if(becameVisible && AudioController.get().isSongNull() && act instanceof SingleAudioPlayActivity) {
                Intent i = new Intent(mActivity, SingleAudioActivity.class);
                i.putExtra("NavItemID", mActivity.getNavItemID());
                mActivity.startActivity(i);
                mActivity.finish();
            }
        });
        mActivity.findViewById(R.id.pa_btnStartTimeDec).setOnClickListener(v -> {
            AudioController.get().setSongStartTime(AudioController.get().getSongStartTime() - UserSettings.SongIncDecInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnStartTimeInc).setOnClickListener(v -> {
            AudioController.get().setSongStartTime(AudioController.get().getSongStartTime() + UserSettings.SongIncDecInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnEndTimeDec).setOnClickListener(v -> {
            AudioController.get().setSongEndTime(AudioController.get().getSongEndTime() - UserSettings.SongIncDecInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnEndTimeInc).setOnClickListener(v -> {
            AudioController.get().setSongEndTime(AudioController.get().getSongEndTime() + UserSettings.SongIncDecInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mLblStartTime.setOnClickListener(v -> {
            AudioController.get().setSongStartTime(mSbProgress.getProgress() * UserSettings.AudioUpdateInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mLblEndTime.setOnClickListener(v -> {
            AudioController.get().setSongEndTime(mSbProgress.getProgress() * UserSettings.AudioUpdateInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnSave).setOnClickListener(this::onLoopSave);
        ((TextView)mActivity.findViewById(R.id.pa_txtInterval)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                UserSettings.SongIncDecInterval = !s.toString().isEmpty() ? Integer.parseInt(s.toString()) : 500;
                try {
                    UserSettings.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(mActivity.findViewById(R.id.pa_scrollview), "Unable to save settings: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // Perform click to update image
        mBtnPlayPause.performClick();
        mBtnPlayPause.performClick();

        // Call listeners to set label text
        mSbStartTime.setProgress(1);
        mSbStartTime.setProgress(AudioController.get().getSongStartTime() != 0 ? AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval : 0);
        mSbEndTime.setProgress(AudioController.get().getSongEndTime() != 0 ? AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval : 0);
    }

    private void onLoopSave(View v) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(R.string.dialog_loop_name)
                .setView(mActivity.getLayoutInflater().inflate(R.layout.save_loop_alert_dialog_layout, null))
                .setPositiveButton(R.string.save, (dialog, id) -> {

                    String loopName = ((EditText)((AlertDialog)dialog).findViewById(R.id.dialog_txtLoopName)).getText().toString();
                    if(loopName.isEmpty() || loopName.contains("_")) {
                        Snackbar.make(v, "Failed to save loop: Name mustn't contain '_' or be empty", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        AudioController.get().saveAsLoop(loopName);
                    } catch (IOException e) {
                        Snackbar.make(v, "Failed to save loop: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    MediaLibrary.loadAvailableLoops();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    dialog.dismiss();
                });
        builder.create().show();
    }
}
