package com.de.mucify.ui;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.common.MediaLibrary;
import com.de.mucify.R;
import com.de.common.UserData;
import com.de.common.Util;
import com.de.common.player.Playback;
import com.de.common.player.Playlist;
import com.de.common.player.Song;
import com.de.mucify.ui.adapter.PlaybackListItemAdapter;

import java.util.ArrayList;

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
    private TextView mTxtProgress;

    private int mPlaybackSeekPos = 0;
    private boolean mIsSeeking = false;

    private final Handler mHandler = new Handler();

    private final ArrayList<Playback> mPlaybacks = new ArrayList<>();


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
        mTxtProgress = findViewById(R.id.txtPos);
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

        mBtnPlayPause.setOnClickListener(v -> {
            if (isPaused())
                unpause();
            else
                pause();
        });

        mBtnPrevious.setOnClickListener(v -> previous());
        mBtnNext.setOnClickListener(v -> next());

        // RecyclerView items
        {
            mRvPlaylistItems.setLayoutManager(new LinearLayoutManager(this));

            ArrayList<Song> songs = ((Playlist) MediaLibrary.getPlaybackFromMediaId(getMediaId())).getSongs();
            mPlaybacks.clear();
            mPlaybacks.addAll(songs);
            PlaybackListItemAdapter adapter = new PlaybackListItemAdapter(this, mPlaybacks);
            adapter.setListener(new AdapterEventListener());
            mRvPlaylistItems.setAdapter(adapter);
        }

        // Update play/pause button image
        if (!isCreated() || isPaused())
            mPlaybackCallback.onPause();
        else
            mPlaybackCallback.onPlay();
        updatePerSongData();

        // Update data with current playlist and song
        updatePerSongData();
        mTxtPlaylistTitle.setText(getPlaylistName());
        mTxtSongCount.setText(getString(R.string.playlist_song_count_text, getSongCountInPlaylist()));
        mTxtTotalPlaylistLength.setText(Util.millisecondsToReadableString(getTotalPlaylistLength()));
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
        }

        @Override
        public void onPlaybackInPlaylistChanged(String mediaId) {
            updatePerSongData();
        }
    }

    /**
     * Should be called whenever a new song is started
     */
    private void updatePerSongData() {
        int duration = isCreated() ? getDuration() / UserData.getAudioUpdateInterval() : 0;
        mSbProgress.setMax(duration);

        mAlbumArt.setImageBitmap(getImage());
        mTxtSongTitle.setText(getSongTitle());
    }

    private class AdapterEventListener extends com.de.mucify.ui.adapter.AdapterEventListener {
        @Override
        public void onClick(RecyclerView.ViewHolder holder, int viewType) {
            skipToPlaylistSong(mPlaybacks.get(holder.getAdapterPosition()).getMediaId());
        }
    }
}
