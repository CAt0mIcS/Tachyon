package com.example.mucify.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.MainActivity;
import com.example.mucify.R;
import com.example.mucify.program_objects.Song;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SingleSongFragment extends Fragment {

    private View mView;
    private MainActivity mActivity;

    private boolean mProgressSeekbarUpdate = true;
    private boolean mStartTimeSeekbarUpdate = true;
    private boolean mEndTimeSeekbarUpdate = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.single_song_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        mView = view;
        SetVisibilities(View.INVISIBLE);

        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
                UpdateSeekbars();
            }
        }, 0, 100);

        ((SeekBar)mView.findViewById(R.id.ssf_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long millis = progress % 1000;
                long second = (progress / 1000) % 60;
                long minute = (progress / (1000 * 60)) % 60;
                long hour = (progress / (1000 * 60 * 60)) % 24;

                ((TextView)mView.findViewById(R.id.ssf_lblProgress)).setText(String.format("%02d:%02d:%02d.%d", hour, minute, second, millis));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = true;
                mActivity.CurrentSong.seekTo(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ssf_sbStartTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mStartTimeSeekbarUpdate = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mStartTimeSeekbarUpdate = true;
                mActivity.CurrentSong.setStartTime(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ssf_sbEndTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mEndTimeSeekbarUpdate = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mEndTimeSeekbarUpdate = true;
                mActivity.CurrentSong.setEndTime(seekBar.getProgress());
            }
        });


        mView.findViewById(R.id.ssf_btnStartTimeDec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.CurrentSong.setStartTime(mActivity.CurrentSong.getStartTime() - getInterval());
            }
        });

        mView.findViewById(R.id.ssf_btnStartTimeInc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.CurrentSong.setStartTime(mActivity.CurrentSong.getStartTime() + getInterval());
            }
        });


        mView.findViewById(R.id.ssf_btnEndTimeDec).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.CurrentSong.setEndTime(mActivity.CurrentSong.getEndTime() - getInterval());
            }
        });

        mView.findViewById(R.id.ssf_btnEndTimeInc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.CurrentSong.setEndTime(mActivity.CurrentSong.getEndTime() + getInterval());
            }
        });
    }

    public void openSong(MainActivity activity) {
        mActivity = activity;
        SetVisibilities(View.VISIBLE);

        ((TextView)mView.findViewById(R.id.ssf_lblSongName)).setText(mActivity.CurrentSong.Name);

        SeekBar startTimeSlider = mView.findViewById(R.id.ssf_sbStartTime);
        startTimeSlider.setMax(mActivity.CurrentSong.getDuration());
        startTimeSlider.setProgress(mActivity.CurrentSong.getStartTime());

        SeekBar endTimeSlider = mView.findViewById(R.id.ssf_sbEndTime);
        endTimeSlider.setMax(mActivity.CurrentSong.getDuration());
        endTimeSlider.setProgress(mActivity.CurrentSong.getEndTime());

        SeekBar progress = mView.findViewById(R.id.ssf_sbProgress);
        progress.setMax(mActivity.CurrentSong.getDuration());
        progress.setProgress(0);
    }

    public int getInterval() {
        return Integer.parseInt(((EditText)mView.findViewById(R.id.ssf_txtInterval)).getText().toString());
    }


    private void SetVisibilities(int visibility) {
        mView.findViewById(R.id.ssf_sbStartTime).setVisibility(visibility);
        mView.findViewById(R.id.ssf_sbProgress).setVisibility(visibility);
        mView.findViewById(R.id.ssf_sbEndTime).setVisibility(visibility);

        mView.findViewById(R.id.ssf_lblSongName).setVisibility(visibility);
        mView.findViewById(R.id.ssf_lblProgress).setVisibility(visibility);

        mView.findViewById(R.id.ssf_txtInterval).setVisibility(visibility);
        mView.findViewById(R.id.ssf_lblTxtInterval).setVisibility(visibility);

        mView.findViewById(R.id.ssf_btnStartTimeDec).setVisibility(visibility);
        mView.findViewById(R.id.ssf_btnStartTimeInc).setVisibility(visibility);
        mView.findViewById(R.id.ssf_btnEndTimeDec).setVisibility(visibility);
        mView.findViewById(R.id.ssf_btnEndTimeInc).setVisibility(visibility);
    }

    private void UpdateSeekbars() {
        if(mActivity != null && mActivity.CurrentSong != null) {
            SeekBar progress = mView.findViewById(R.id.ssf_sbProgress);
            SeekBar startTime = mView.findViewById(R.id.ssf_sbStartTime);
            SeekBar endTime = mView.findViewById(R.id.ssf_sbEndTime);

            if(mProgressSeekbarUpdate)
                progress.setProgress(mActivity.CurrentSong.getCurrentPosition());

            if(mStartTimeSeekbarUpdate)
                startTime.setProgress(mActivity.CurrentSong.getStartTime());

            if(mEndTimeSeekbarUpdate)
                endTime.setProgress(mActivity.CurrentSong.getEndTime());
        }
    }
}
