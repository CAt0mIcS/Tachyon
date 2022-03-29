package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

public class ActivityPlaylistPlayer extends MediaControllerActivity {
    private final PlaybackCallback mPlaybackCallback = new PlaybackCallback();

    private ImageView mAlbumArt;
    private TextView mTxtPlaylistTitle;
    private TextView mTxtSongCount;
    private TextView mTxtTotalPlaylistLength;
    private RecyclerView mRvPlaylistItems;
    private ImageView mBtnPlayPause;
    private ImageView mBtnPrevious;
    private ImageView mBtnNext;
    private TextView mTxtSongTitle;
    private SeekBar mSbProgress;

    private int mPlaybackSeekPos = 0;
    private boolean mIsSeeking = false;

    private final Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_player);

        addCallback(mPlaybackCallback);

        mAlbumArt = findViewById(R.id.album_art);
        mTxtPlaylistTitle = findViewById(R.id.txtPlaylistTitle);
        mTxtSongCount = findViewById(R.id.txtSongCount);
        mTxtTotalPlaylistLength = findViewById(R.id.txtTotalPlaylistLength);
        mRvPlaylistItems = findViewById(R.id.rvPlaylistItems);
        mBtnPlayPause = findViewById(R.id.btnPlayPause);
        mBtnNext = findViewById(R.id.btnNext);
        mBtnPrevious = findViewById(R.id.btnPrevious);
        mTxtSongTitle = findViewById(R.id.txtSongTitle);
        mSbProgress = findViewById(R.id.sbPos);
    }

    @Override
    public void onConnected() {
        if (!getIntent().getBooleanExtra("IsPlaying", false) &&
                getIntent().getStringExtra("MediaId") != null)
            play(getIntent().getStringExtra("MediaId"));

        mPlaybackSeekPos = getIntent().getIntExtra("SeekPos", 0);

        // MY_TODO: Shouldn't access media library here
        {
            Playlist playlist = (Playlist) MediaLibrary.getPlaybackFromMediaId(
                    getIntent().getStringExtra("MediaId"));
            mTxtPlaylistTitle.setText(playlist.getName());
            mTxtSongCount.setText(playlist.getSongs().size() + " " + getString(R.string.songs));
            mTxtTotalPlaylistLength.setText(calculateTotalPlaylistLength(playlist));
        }

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
            }
        });

        mBtnPlayPause.setOnClickListener(v -> {
            if (!isCreated()) {
                play(getIntent().getStringExtra("MediaId"));
                if (mPlaybackSeekPos != 0)
                    seekTo(mPlaybackSeekPos);
            } else if (isPaused())
                unpause();
            else
                pause();
        });

        mBtnPrevious.setOnClickListener(v -> previous());
        mBtnNext.setOnClickListener(v -> next());


        // Update play/pause button image
        if (!isCreated() || isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onStart();
        updatePerSongData();

        // Call the event handlers once to set all the values to the current song
        if (isCreated()) {
            mPlaybackCallback.onTitleChanged(getSongTitle());
            mPlaybackCallback.onArtistChanged(getSongArtist());
        } else {
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
            if (mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.pause);
        }

        @Override
        public void onPause() {
            if (mBtnPlayPause != null)
                mBtnPlayPause.setImageResource(R.drawable.play);
        }

        @Override
        public void onTitleChanged(String title) {
            mTxtSongTitle.setText(title);
        }

        @Override
        public void onSeekTo(int millis) {
            mSbProgress.setProgress(millis / UserData.getAudioUpdateInterval());
        }

        @Override
        public void onMediaIdChanged(String mediaId) {
            getIntent().putExtra("MediaId", mediaId);
        }

        @Override
        public void onPlaylistSongChanged(String mediaId) {
            updatePerSongData();
        }
    }

    /**
     * Should be called whenever a new song is started
     */
    private void updatePerSongData() {
        int duration = isCreated() ? getDuration() / UserData.getAudioUpdateInterval() : 0;
        mSbProgress.setMax(duration);

        // MY_TODO: Shouldn't access media library here
        mAlbumArt.setImageBitmap(MediaLibrary.getPlaybackFromMediaId(
                getIntent().getStringExtra("MediaId")).getCurrentSong().getImage());
    }

    /**
     * Calculates the total length of the playlist. This means the length of all songs added up.
     * Formats it into a readable string
     */
    private String calculateTotalPlaylistLength(Playlist playlist) {
        int duration = 0;
        for (Song s : playlist.getSongs())
            duration += s.isCreated() ? s.getDuration() : s.getEndTimeUninitialized() - s.getStartTime();

        return Util.millisecondsToReadableString(duration);
    }
}
