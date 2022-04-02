package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Song;
import com.de.mucify.ui.trivial.DialogAddToPlaylist;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        setContentView(R.layout.activity_player);

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
        if (getIntent().getBooleanExtra("StartPlaying", false))
            play();

        mPlaybackSeekPos = getIntent().getIntExtra("SeekPos", 0);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isCreated() && isPlaying() && !mIsSeeking) {
                    int currentPos = getCurrentPosition() / UserData.getAudioUpdateInterval();
                    mSbProgress.setProgress(currentPos);
                }
                if (!isDestroyed())
                    mHandler.postDelayed(this, UserData.getAudioUpdateInterval());
            }
        });

        ((BottomNavigationView) findViewById(R.id.btmNavPlayer)).setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.btmNavAddToPlaylist)
                new DialogAddToPlaylist((Song) MediaLibrary.getPlaybackFromMediaId(
                        getCurrentSongMediaId()), ActivityPlayer.this).show();
            else if (item.getItemId() == R.id.btmNavSaveLoop)
                displaySaveLoopDialog();

            return true;
        });

        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsSeeking = false;
                seekTo(seekBar.getProgress() * UserData.getAudioUpdateInterval());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsSeeking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtProgress.setText(Util.millisecondsToReadableString(progress * UserData.getAudioUpdateInterval()));
            }
        });
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserData.getAudioUpdateInterval();
                setStartTime(startTime);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtStartTime.setText(Util.millisecondsToReadableString(progress * UserData.getAudioUpdateInterval()));
            }
        });
        mSbEndTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = seekBar.getProgress() * UserData.getAudioUpdateInterval();
                setEndTime(endTime);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtEndTime.setText(Util.millisecondsToReadableString(progress * UserData.getAudioUpdateInterval()));
            }
        });

        findViewById(R.id.btnStartPosDec).setOnClickListener(v -> {
            int time = getStartTime() - UserData.getSongIncDecInterval();
            if (time < 0)
                time = 0;

            setStartTime(time);
            mSbStartTime.setProgress(time / UserData.getAudioUpdateInterval());
        });
        findViewById(R.id.btnStartPosInc).setOnClickListener(v -> {
            int time = getStartTime() + UserData.getSongIncDecInterval();
            if (time > getDuration())
                time = getDuration();

            setStartTime(time);
            mSbStartTime.setProgress(time / UserData.getAudioUpdateInterval());
        });
        findViewById(R.id.btnEndPosDec).setOnClickListener(v -> {
            int time = getEndTime() - UserData.getSongIncDecInterval();
            if (time < 0)
                time = 0;

            setEndTime(time);
            mSbEndTime.setProgress(time / UserData.getAudioUpdateInterval());
        });
        findViewById(R.id.btnEndPosInc).setOnClickListener(v -> {
            int time = getEndTime() + UserData.getSongIncDecInterval();
            if (time > getDuration())
                time = getDuration();

            setEndTime(time);
            mSbEndTime.setProgress(time / UserData.getAudioUpdateInterval());
        });

        mLayoutStartPos.setOnClickListener(v -> {
            setStartTime(mSbProgress.getProgress() * UserData.getAudioUpdateInterval());
            mSbStartTime.setProgress(getStartTime() / UserData.getAudioUpdateInterval());
        });
        mLayoutEndPos.setOnClickListener(v -> {
            setEndTime(mSbProgress.getProgress() * UserData.getAudioUpdateInterval());
            mSbEndTime.setProgress(getEndTime() / UserData.getAudioUpdateInterval());
        });

        mBtnPlayPause.setOnClickListener(v -> {
            if (isPaused())
                unpause();
            else
                pause();
        });

        // Update play/pause button image
        if (!isCreated() || isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onPlay();
        updatePerSongData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeCallback(mPlaybackCallback);
    }

    private class PlaybackCallback extends Callback {
        @Override
        public void onPlay() {
            if (mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.pause);

            if (mPlaybackSeekPos != 0) {
                seekTo(mPlaybackSeekPos);
                mPlaybackSeekPos = 0;
            }
        }

        @Override
        public void onPause() {
            if (mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.play);
        }

        @Override
        public void onSeekTo(int millis) {
            mSbProgress.setProgress(millis / UserData.getAudioUpdateInterval());
        }

        @Override
        public void onMediaIdChanged(String mediaId) {
            getIntent().putExtra("MediaId", mediaId);
            updatePerSongData();
        }
    }

    /**
     * Should be called whenever a new song is started
     */
    private void updatePerSongData() {
        int duration = getDuration() / UserData.getAudioUpdateInterval();
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        mSbStartTime.setProgress(getStartTime() / UserData.getAudioUpdateInterval());
        mSbEndTime.setProgress(getEndTime() / UserData.getAudioUpdateInterval());

        mTxtTitle.setText(getSongTitle());
        mTxtSubtitle.setText(getSongArtist());
    }

    /**
     * Displays dialog to save loop
     */
    private void displaySaveLoopDialog() {
        final EditText editLoopName = new EditText(this);

        new AlertDialog.Builder(this)
                .setMessage("Enter loop name")
                .setView(editLoopName)
                .setPositiveButton("Save", (dialog, id) -> {

                    String loopName = editLoopName.getText().toString();
                    if (loopName.isEmpty() || loopName.contains("_")) {
                        Toast.makeText(this, "Failed to save loop: Name mustn't contain '_' or be empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    saveAsLoop(loopName);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .create().show();
    }
}
