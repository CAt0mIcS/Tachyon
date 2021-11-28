package com.de.mucify.activity.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;


public class MultiAudioPlayController {
    private final MultiAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();
    private final SeekBar mSbProgress;
    private final TextView mLblProgress;
    private final ImageButton mBtnPlayPause;
    private final TextView mLblSongName;

    private boolean mIsSeeking = false;

    public MultiAudioPlayController(MultiAudioPlayActivity activity) {
        mActivity = activity;

        TelephonyManager mgr = (TelephonyManager)mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        if(mgr != null) {
            // Incoming call || A call is dialing, active or on hold
            mgr.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    // Incoming call || A call is dialing, active or on hold
                    if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        if (!AudioController.get().isSongNull())
                            AudioController.get().pauseSong();
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mSbProgress = mActivity.findViewById(R.id.pp_sbProgress);
        mLblProgress = mActivity.findViewById(R.id.pp_lblProgress);
        mBtnPlayPause = mActivity.findViewById(R.id.pp_btnPause);
        mLblSongName = mActivity.findViewById(R.id.pp_lblSongName);

        AudioController.NextSongListener nextSongListener = nextSong -> {
            mSbProgress.setMax(AudioController.get().getSongDuration() / UserSettings.AudioUpdateInterval);
            mLblSongName.setText(AudioController.get().getSongTitle() + " " + mActivity.getString(R.string.by) + " " + AudioController.get().getSongArtist());
        };
        AudioController.get().addOnNextSongListener(nextSongListener, AudioController.INDEX_DONT_CARE);
        nextSongListener.onNextSong(AudioController.get().getSong());

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!AudioController.get().isSongNull() && AudioController.get().isSongPlaying() && !mIsSeeking) {
                    int mCurrentPosition = AudioController.get().getCurrentSongPosition() / UserSettings.AudioUpdateInterval;
                    mSbProgress.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, UserSettings.AudioUpdateInterval);
            }
        });
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { mIsSeeking = false; AudioController.get().seekSongTo(seekBar.getProgress() * UserSettings.AudioUpdateInterval); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mIsSeeking = true; }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!AudioController.get().isSongNull())
                    mLblProgress.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });
        mBtnPlayPause.setOnClickListener(v -> {
            if(AudioController.get().isSongPlaying())
                AudioController.get().pauseSong();
            else
                AudioController.get().unpauseSong();
        });
        AudioController.get().addOnSongPausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_play_arrow_black), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongUnpausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_pause_black), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongResetListener(song -> {
            if(MucifyApplication.isActivityVisible() && MultiAudioActivity.get() == null) {
                Intent i = new Intent(mActivity, MultiAudioActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            }
        }, AudioController.INDEX_DONT_CARE);
        MucifyApplication.addOnActivityVisibilityChangedListener((act, becameVisible) -> {
            if(becameVisible && AudioController.get().isSongNull() && act instanceof MultiAudioPlayActivity) {
                Intent i = new Intent(mActivity, MultiAudioActivity.class);
                mActivity.startActivity(i);
                mActivity.finish();
            }
        });
    }
}
