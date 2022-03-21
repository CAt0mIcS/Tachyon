package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.ui.trivial.DialogAddToPlaylist;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

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

    private boolean mIsSeeking = false;
    private int mPlaybackSeekPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_layout);

        addCallback(mPlaybackCallback);

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
    public void onConnected() {
        if(!getIntent().getBooleanExtra("IsPlaying", false) && getIntent().getStringExtra("MediaId") != null)
            play(getIntent().getStringExtra("MediaId"));

        mPlaybackSeekPos = getIntent().getIntExtra("SeekPos", 0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isCreated() && isPlaying() && !mIsSeeking) {
                    int currentPos = getCurrentPosition() / UserData.AudioUpdateInterval;
                    mSbProgress.setProgress(currentPos);
                }
                mHandler.postDelayed(this, UserData.AudioUpdateInterval);
            }
        });

        ((BottomNavigationView)findViewById(R.id.btmNavPlayer)).setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.btmNavAddToPlaylist) {
                new DialogAddToPlaylist(MediaLibrary.getPlaybackFromMediaId(getIntent().getStringExtra("MediaId")).getCurrentSong(), ActivityPlayer.this).show();
            }

            return true;
        });

        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { mIsSeeking = false; seekTo(seekBar.getProgress() * UserData.AudioUpdateInterval); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mIsSeeking = true; }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtProgress.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserData.AudioUpdateInterval;
                setStartTime(startTime);
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
                setEndTime(endTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtEndTime.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });

        findViewById(R.id.btnStartPosDec).setOnClickListener(v -> {
            int time = getStartTime() - UserData.SongIncDecInterval;
            if(time < 0)
                time = 0;

            setStartTime(time);
            mSbStartTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnStartPosInc).setOnClickListener(v -> {
            int time = getStartTime() + UserData.SongIncDecInterval;
            if(time > getDuration())
                time = getDuration();

            setStartTime(time);
            mSbStartTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnEndPosDec).setOnClickListener(v -> {
            int time = getEndTime() - UserData.SongIncDecInterval;
            if(time < 0)
                time = 0;

            setEndTime(time);
            mSbEndTime.setProgress(time / UserData.AudioUpdateInterval);
        });
        findViewById(R.id.btnEndPosInc).setOnClickListener(v -> {
            int time = getEndTime() + UserData.SongIncDecInterval;
            if(time > getDuration())
                time = getDuration();

            setEndTime(time);
            mSbEndTime.setProgress(time / UserData.AudioUpdateInterval);
        });

        mLayoutStartPos.setOnClickListener(v -> {
            setStartTime(mSbProgress.getProgress() * UserData.AudioUpdateInterval);
            mSbStartTime.setProgress(getStartTime() / UserData.AudioUpdateInterval);
        });
        mLayoutEndPos.setOnClickListener(v -> {
            setEndTime(mSbProgress.getProgress() * UserData.AudioUpdateInterval);
            mSbEndTime.setProgress(getEndTime() / UserData.AudioUpdateInterval);
        });

        mBtnPlayPause.setOnClickListener(v -> {
            if(!isCreated()) {
                play(getIntent().getStringExtra("MediaId"));
                if(mPlaybackSeekPos != 0)
                    seekTo(mPlaybackSeekPos);
            }

            else if(isPaused())
                unpause();
            else
                pause();
        });

        // Update play/pause button image
        if(!isCreated() || isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onStart();
        updatePerSongData();

        // Call the event handlers once to set all the values to the current song
        if(isCreated()) {
            mPlaybackCallback.onTitleChanged(getSongTitle());
            mPlaybackCallback.onArtistChanged(getSongArtist());
        }
        else {
            mPlaybackCallback.onTitleChanged(getIntent().getStringExtra("Title"));
            mPlaybackCallback.onArtistChanged(getIntent().getStringExtra("Subtitle"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Callback {
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

        @Override
        public void onTitleChanged(String title) {
            mTxtTitle.setText(title);
        }

        @Override
        public void onArtistChanged(String artist) {
            mTxtSubtitle.setText(artist);
        }

        @Override
        public void onSeekTo(int millis) {
            mSbProgress.setProgress(millis / UserData.AudioUpdateInterval);
        }

        @Override
        public void onMediaIdChanged(String mediaId) {
            getIntent().putExtra("MediaId", mediaId);
        }
    }

    /**
     * Should be called whenever a new song is started
     */
    private void updatePerSongData() {
        int duration = isCreated() ? getDuration() / UserData.AudioUpdateInterval : 0;
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        mSbStartTime.setProgress(isCreated() ? getStartTime() / UserData.AudioUpdateInterval : 0);
        mSbEndTime.setProgress(isCreated() ? getEndTime() / UserData.AudioUpdateInterval : 0);
    }
}
