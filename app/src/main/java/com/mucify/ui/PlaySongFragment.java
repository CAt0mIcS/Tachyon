package com.mucify.ui;

import android.app.ActivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mucify.Globals;
import com.mucify.R;
import com.mucify.Utils;
import com.mucify.objects.Loop;
import com.mucify.objects.Song;

import java.io.File;
import java.io.IOException;

public class PlaySongFragment extends Fragment {
    private View mView;
    private final Song mSong;

    private boolean mProgressSeekbarUpdate = true;

    public PlaySongFragment(Song song) {
        mSong = song;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.play_song_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mView = view;
        mSong.start();

        // Repeat song once it's finished
        mSong.setOnMediaPlayerFinishedListener(song -> {
            song.seekTo(mSong.getStartTime());
            song.start();
        });

        // Set maximum values for seekbars
        ((SeekBar)mView.findViewById(R.id.ps_sbProgress)).setMax(mSong.getDuration());
        SeekBar sbStartTime = mView.findViewById(R.id.ps_sbStartTime);
        SeekBar sbEndTime = mView.findViewById(R.id.ps_sbEndTime);
        sbStartTime.setMax(mSong.getDuration());
        sbEndTime.setMax(mSong.getDuration());

        // Update progress seekbar with media player position as well as update the song
        mView.findViewById(R.id.ps_sbProgress).post(new Runnable() {
            @Override
            public void run() {
                mSong.update();

                SeekBar progress = mView.findViewById(R.id.ps_sbProgress);
                if(progress == null)
                    return;

                if(mSong.isPlaying()) {
                    if(mProgressSeekbarUpdate) {
                        progress.setProgress(mSong.getCurrentPosition());
                    }
                    progress.post(this);
                }
                else
                    progress.postDelayed(this, 100);
            }
        });

        // region Listeners
        ((TextView)mView.findViewById(R.id.ps_lblSongName)).setText(mSong.getName());


        ((SeekBar)mView.findViewById(R.id.ps_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView lblProgress = mView.findViewById(R.id.ps_lblProgress);
                lblProgress.setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mProgressSeekbarUpdate = false; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = true;
                mSong.seekTo(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ps_sbStartTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)mView.findViewById(R.id.ps_lblStartTime)).setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSong.setStartTime(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ps_sbEndTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)mView.findViewById(R.id.ps_lblEndTime)).setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSong.setEndTime(seekBar.getProgress());
            }
        });


        mView.findViewById(R.id.ps_btnStartTimeDec).setOnClickListener(v -> {
            mSong.setStartTime(mSong.getStartTime() - Globals.SongIncDecInterval);
            ((SeekBar)mView.findViewById(R.id.ps_sbStartTime)).setProgress(mSong.getStartTime());
        });

        mView.findViewById(R.id.ps_btnStartTimeInc).setOnClickListener(v -> {
            mSong.setStartTime(mSong.getStartTime() + Globals.SongIncDecInterval);
            ((SeekBar)mView.findViewById(R.id.ps_sbStartTime)).setProgress(mSong.getStartTime());
        });


        mView.findViewById(R.id.ps_btnEndTimeDec).setOnClickListener(v -> {
            mSong.setEndTime(mSong.getEndTime() - Globals.SongIncDecInterval);
            ((SeekBar)mView.findViewById(R.id.ps_sbEndTime)).setProgress(mSong.getEndTime());
        });

        mView.findViewById(R.id.ps_btnEndTimeInc).setOnClickListener(v -> {
            mSong.setEndTime(mSong.getEndTime() + Globals.SongIncDecInterval);
            ((SeekBar)mView.findViewById(R.id.ps_sbEndTime)).setProgress(mSong.getEndTime());
        });

        mView.findViewById(R.id.ps_btnSave).setOnClickListener(this::onLoopSaveClicked);

        mView.findViewById(R.id.ps_lblStartTime).setOnClickListener(v -> {
            mSong.setStartTime(((SeekBar)mView.findViewById(R.id.ps_sbProgress)).getProgress());
            ((SeekBar)mView.findViewById(R.id.ps_sbStartTime)).setProgress(mSong.getStartTime());
        });

        mView.findViewById(R.id.ps_lblEndTime).setOnClickListener(v -> {
            mSong.setEndTime(((SeekBar)mView.findViewById(R.id.ps_sbProgress)).getProgress());
            ((SeekBar)mView.findViewById(R.id.ps_sbEndTime)).setProgress(mSong.getEndTime());
        });

        ((EditText)mView.findViewById(R.id.ps_txtInterval)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Globals.SongIncDecInterval = Integer.parseInt(s.toString());
            }
        });
        //endregion

        // Set progresses of seekbars so that the listeners get activated and the labels are updated
        sbStartTime.setProgress(1);
        sbStartTime.setProgress(mSong.getStartTime());
        sbEndTime.setProgress(mSong.getEndTime());
    }

    private void onLoopSaveClicked(View view) {
        String name = "Test";

        try {
            Loop.save(mSong, Loop.toFile(name));
        } catch(IOException e) {
            Utils.messageBox(getContext(), "Failed to save loop " + name, e.getMessage());
        }
    }

    public void unload() {
        mSong.pause();
        mSong.seekTo(0);
    }
}
