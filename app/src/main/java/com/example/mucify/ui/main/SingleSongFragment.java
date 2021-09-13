package com.example.mucify.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mucify.MainActivity;
import com.example.mucify.R;
import com.example.mucify.program_objects.Song;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class SingleSongFragment extends Fragment {

    private View mView;
    private MainActivity mActivity;

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
        }, 0, 1000);

        ((SeekBar)mView.findViewById(R.id.ssf_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mActivity.CurrentSong.seekTo(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ssf_sbStartTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mActivity.CurrentSong.setStartTime(seekBar.getProgress());
            }
        });

        ((SeekBar)mView.findViewById(R.id.ssf_sbEndTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mActivity.CurrentSong.setEndTime(seekBar.getProgress());
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


    private void SetVisibilities(int visibility) {
        mView.findViewById(R.id.ssf_sbStartTime).setVisibility(visibility);
        mView.findViewById(R.id.ssf_sbProgress).setVisibility(visibility);
        mView.findViewById(R.id.ssf_sbEndTime).setVisibility(visibility);

        mView.findViewById(R.id.ssf_lblSongName).setVisibility(visibility);
    }

    private void UpdateSeekbars() {
        if(mActivity != null && mActivity.CurrentSong != null) {
            ((SeekBar)mView.findViewById(R.id.ssf_sbProgress)).setProgress(mActivity.CurrentSong.getCurrentPosition());
            ((SeekBar)mView.findViewById(R.id.ssf_sbStartTime)).setProgress(mActivity.CurrentSong.getStartTime());
            ((SeekBar)mView.findViewById(R.id.ssf_sbEndTime)).setProgress(mActivity.CurrentSong.getEndTime());
        }
    }
}
