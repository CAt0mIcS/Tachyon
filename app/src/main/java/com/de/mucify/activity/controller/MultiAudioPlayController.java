package com.de.mucify.activity.controller;

import android.content.Intent;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.adapter.LoopListItemAdapter;
import com.de.mucify.adapter.SongListItemAdapter;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.PlaylistAudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MultiAudioPlayController {
    private final MultiAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();

    private final ArrayList<Song> mSongs;
    private final ArrayList<Song> mLoops;

    private final TextView mLblName;
    private final SeekBar mSbProgress;
    private final TextView mLblProgress;
    private final RecyclerView mRvSongs;
    private final RecyclerView mRvLoops;

    private boolean mIsSeeking = false;

    public MultiAudioPlayController(MultiAudioPlayActivity activity) {
        mActivity = activity;

        mLblName = mActivity.findViewById(R.id.pp_lblSongName);
        mSbProgress = mActivity.findViewById(R.id.pp_sbProgress);
        mLblProgress = mActivity.findViewById(R.id.pp_lblProgress);
        mRvSongs = mActivity.findViewById(R.id.pp_rvSongs);
        mRvLoops = mActivity.findViewById(R.id.pp_rvLoops);

        mSongs = PlaylistAudioController.get().getSongs();
        mLoops = PlaylistAudioController.get().getLoops();

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
                if(!AudioController.get().isSongNull() && AudioController.get().isSongPlaying() && !mIsSeeking) {
                    int mCurrentPosition = AudioController.get().getCurrentSongPosition() / UserSettings.AudioUpdateInterval;
                    mSbProgress.setProgress(mCurrentPosition);
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
            public void onStopTrackingTouch(SeekBar seekBar) { mIsSeeking = false; AudioController.get().seekSongTo(seekBar.getProgress() * UserSettings.AudioUpdateInterval); }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mIsSeeking = true; }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(!AudioController.get().isSongNull())
                    mLblProgress.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });

        mRvSongs.setLayoutManager(new LinearLayoutManager(mActivity));
        SongListItemAdapter adapter = new SongListItemAdapter(mActivity, mSongs);
//        adapter.setOnItemClicked(this::onFileClicked);
        mRvSongs.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mRvLoops.setLayoutManager(new LinearLayoutManager(mActivity));
        LoopListItemAdapter adapter2 = new LoopListItemAdapter(mActivity, mLoops);
//        adapter2.setOnItemClicked(this::onFileClicked);
        mRvLoops.setAdapter(adapter2);
        adapter2.notifyDataSetChanged();
    }
}
