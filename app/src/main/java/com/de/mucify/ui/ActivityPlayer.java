package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Song;

public class ActivityPlayer extends MediaControllerActivity {
    private SeekBar mSbProgress;
    private SeekBar mSbStartTime;
    private SeekBar mSbEndTime;
    private TextView mTxtProgress;
    private TextView mTxtStartTime;
    private TextView mTxtEndTime;
    private TextView mTxtTitle;
    private TextView mTxtSubtitle;
    private LinearLayout mLayoutStartPos;
    private LinearLayout mLayoutEndPos;
    private ImageButton mBtnPlayPause;

    private final Handler mHandler = new Handler();
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();
    private Song mSong;

    private boolean mIsSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_layout);

        mSbProgress = findViewById(R.id.sbPos);
        mSbStartTime = findViewById(R.id.sbStartPos);
        mSbEndTime = findViewById(R.id.sbEndPos);
        mTxtProgress = findViewById(R.id.txtPos);
        mTxtStartTime = findViewById(R.id.txtStartPos);
        mTxtEndTime = findViewById(R.id.txtEndPos);
        mTxtTitle = findViewById(R.id.txtTitle);
        mTxtSubtitle = findViewById(R.id.txtArtist);
        mLayoutStartPos = findViewById(R.id.linearLayoutStartPos);
        mLayoutEndPos = findViewById(R.id.linearLayoutEndPos);
        mBtnPlayPause = findViewById(R.id.btnPlayPause);
    }

    @Override
    public void onBuildTransportControls() {
        if(!getIntent().getBooleanExtra("IsPlaying", false))
            play(getIntent().getStringExtra("MediaId"));

        mSong = (Song)Util.getPlaybackFromMediaId(getIntent().getStringExtra("MediaId"));
        mSong.addCallback(mPlaybackCallback);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mSong != null && mSong.isCreated() && mSong.isPlaying() && !mIsSeeking) {
                    int currentPos = mSong.getCurrentPosition() / UserData.AudioUpdateInterval;
                    mSbProgress.setProgress(currentPos);
                }
                mHandler.postDelayed(this, UserData.AudioUpdateInterval);
            }
        });

        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { mIsSeeking = false; seekTo(seekBar.getProgress() * UserData.AudioUpdateInterval); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mIsSeeking = true; }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mSong != null)
                    mTxtProgress.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserData.AudioUpdateInterval;
                mSong.setStartTime(startTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtStartTime.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });
        mSbEndTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = seekBar.getProgress() * UserData.AudioUpdateInterval;
                mSong.setEndTime(endTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtEndTime.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });

        findViewById(R.id.btnStartPosDec).setOnClickListener(v -> {
            int time = mSong.getStartTime() - UserData.SongIncDecInterval;
            if(time < 0)
                time = 0;

            mSong.setStartTime(time);
            mSbStartTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnStartPosInc).setOnClickListener(v -> {
            int time = mSong.getStartTime() + UserData.SongIncDecInterval;
            if(time > mSong.getDuration())
                time = mSong.getDuration();

            mSong.setStartTime(time);
            mSbStartTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnEndPosDec).setOnClickListener(v -> {
            int time = mSong.getEndTime() - UserData.SongIncDecInterval;
            if(time < 0)
                time = 0;

            mSong.setEndTime(time);
            mSbEndTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnEndPosInc).setOnClickListener(v -> {
            int time = mSong.getEndTime() + UserData.SongIncDecInterval;
            if(time > mSong.getDuration())
                time = mSong.getDuration();

            mSong.setEndTime(time);
            mSbEndTime.setProgress(time / UserData.AudioUpdateInterval);
        });

        mLayoutStartPos.setOnClickListener(v -> {
            mSong.setStartTime(mSbProgress.getProgress() * UserData.AudioUpdateInterval);
            mSbStartTime.setProgress(mSong.getStartTime() / UserData.AudioUpdateInterval);
        });
        mLayoutEndPos.setOnClickListener(v -> {
            mSong.setEndTime(mSbProgress.getProgress() * UserData.AudioUpdateInterval);
            mSbEndTime.setProgress(mSong.getEndTime() / UserData.AudioUpdateInterval);
        });

        mBtnPlayPause.setOnClickListener(v -> {
            if(!mSong.isCreated())
                play(mSong);
            else if(mSong.isPaused())
                unpause();
            else
                pause();
        });

        // Update play/pause button image
        if(!mSong.isCreated() || mSong.isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onStart();
        updatePerSongData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSong.removeCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Playback.Callback {
        @Override
        public void onNext(Song next) {
            mSong = next;
            updatePerSongData();
        }

        @Override
        public void onPrevious(Song previous) {
            mSong = previous;
            updatePerSongData();
        }

        @Override
        public void onStart() {
            if(mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.pause);
            updatePerSongData();
        }

        @Override
        public void onPause() {
            if(mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.play);
        }
    }

    private void updatePerSongData() {
        mTxtTitle.setText(mSong.getTitle());
        mTxtSubtitle.setText(mSong.getSubtitle());

        int duration = mSong.isCreated() ? mSong.getDuration() / UserData.AudioUpdateInterval : 0;
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        mSbStartTime.setProgress(mSong.isCreated() ? mSong.getStartTime() / UserData.AudioUpdateInterval : 0);
        mSbEndTime.setProgress(mSong.isCreated() ? mSong.getEndTime() / UserData.AudioUpdateInterval : 0);
    }
}
