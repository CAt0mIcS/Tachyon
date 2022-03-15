package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
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

    private final Handler mHandler = new Handler();
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();
    private Playback mPlayback;

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
    }

    @Override
    public void onBuildTransportControls() {
        if(!getIntent().getBooleanExtra("IsPlaying", false))
            play(getIntent().getStringExtra("MediaId"));

        mPlayback = Util.getPlaybackFromMediaId(getIntent().getStringExtra("MediaId"));
        mPlayback.addCallback(mPlaybackCallback);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mPlayback != null && mPlayback.isPlaying() && !mIsSeeking) {
                    int currentPos = mPlayback.getCurrentPosition() / UserData.AudioUpdateInterval;
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
                if(mPlayback != null)
                    mTxtProgress.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserData.AudioUpdateInterval;
                mPlayback.setStartTime(startTime);
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
                mPlayback.setEndTime(endTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtEndTime.setText(Util.millisecondsToReadableString(progress * UserData.AudioUpdateInterval));
            }
        });

        updatePerSongData(mPlayback.getCurrentSong());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayback.removeCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Playback.Callback {
        @Override
        public void onNext(Song next) {
            mPlayback = next;
            updatePerSongData(next);
        }

        @Override
        public void onPrevious(Song previous) {
            mPlayback = previous;
            updatePerSongData(previous);
        }
    }

    private void updatePerSongData(Song song) {
        mTxtTitle.setText(song.getTitle());
        mTxtSubtitle.setText(song.getSubtitle());

        int duration = song.getDuration() / UserData.AudioUpdateInterval;
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        mSbStartTime.setProgress(song.getStartTime() != 0 ? song.getStartTime() / UserData.AudioUpdateInterval : 0);
        mSbEndTime.setProgress(song.getEndTime() != 0 ? song.getEndTime() / UserData.AudioUpdateInterval : 0);
    }
}
