package com.mucify.ui.fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
import com.mucify.ui.internal.PlaySongService;

import java.io.IOException;

public class PlaySongFragment extends Fragment {
    private boolean mProgressSeekbarUpdate = true;
    private Intent mForegroundIntent;

    private final BroadcastReceiver mNoisyAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
                Song.get().pause();
        }
    };
    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.play_audio_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mForegroundIntent = new Intent(getActivity(), PlaySongService.class);

        // Repeat song once it's finished
        Song.get().addOnMediaPlayerFinishedListener(song -> {
            song.start();
        });
        Song.get().addOnMediaPlayerStoppedListener(song -> {
            getContext().unregisterReceiver(mNoisyAudioReceiver);
        });
        Song.get().addOnMediaPlayerStartedListener(song -> {
            if(!isServiceRunning(PlaySongService.class))
                getActivity().startService(mForegroundIntent);
            getContext().registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);
        });

        Song.get().start();

        // Set maximum values for seekbars
        SeekBar sbProgress = view.findViewById(R.id.pa_sbProgress);
        EditText txtInterval = view.findViewById(R.id.pa_txtInterval);
        SeekBar sbStartTime = view.findViewById(R.id.pa_sbStartTime);
        SeekBar sbEndTime = view.findViewById(R.id.pa_sbEndTime);
        sbProgress.setMax(Song.get().getDuration());
        sbStartTime.setMax(Song.get().getDuration());
        sbEndTime.setMax(Song.get().getDuration());
        txtInterval.setText(String.valueOf(Globals.SongIncDecInterval));

        // Update progress seekbar with media player position as well as update the song
        sbProgress.post(new Runnable() {
            @Override
            public void run() {
                Song.get().update();

                SeekBar progress = view.findViewById(R.id.pa_sbProgress);
                if(progress == null)
                    return;

                if(Song.get().isPlaying()) {
                    if(mProgressSeekbarUpdate) {
                        progress.setProgress(Song.get().getCurrentPosition());
                    }
                    progress.post(this);
                }
                else
                    progress.postDelayed(this, 100);
            }
        });

        // region Listeners
        ((TextView)view.findViewById(R.id.pa_lblSongName)).setText(Song.get().getName());


        ((SeekBar)view.findViewById(R.id.pa_sbProgress)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView lblProgress = view.findViewById(R.id.pa_lblProgress);
                lblProgress.setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { mProgressSeekbarUpdate = false; }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressSeekbarUpdate = true;
                Song.get().seekTo(seekBar.getProgress());
            }
        });

        ((SeekBar)view.findViewById(R.id.pa_sbStartTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)view.findViewById(R.id.pa_lblStartTime)).setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Song.get().setStartTime(seekBar.getProgress());
            }
        });

        ((SeekBar)view.findViewById(R.id.pa_sbEndTime)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView)view.findViewById(R.id.pa_lblEndTime)).setText(Utils.millisecondsToReadableString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Song.get().setEndTime(seekBar.getProgress());
            }
        });


        view.findViewById(R.id.pa_btnStartTimeDec).setOnClickListener(v -> {
            Song.get().setStartTime(Song.get().getStartTime() - Globals.SongIncDecInterval);
            ((SeekBar)view.findViewById(R.id.pa_sbStartTime)).setProgress(Song.get().getStartTime());
        });

        view.findViewById(R.id.pa_btnStartTimeInc).setOnClickListener(v -> {
            Song.get().setStartTime(Song.get().getStartTime() + Globals.SongIncDecInterval);
            ((SeekBar)view.findViewById(R.id.pa_sbStartTime)).setProgress(Song.get().getStartTime());
        });

        view.findViewById(R.id.pa_btnEndTimeDec).setOnClickListener(v -> {
            Song.get().setEndTime(Song.get().getEndTime() - Globals.SongIncDecInterval);
            ((SeekBar)view.findViewById(R.id.pa_sbEndTime)).setProgress(Song.get().getEndTime());
        });

        view.findViewById(R.id.pa_btnEndTimeInc).setOnClickListener(v -> {
            Song.get().setEndTime(Song.get().getEndTime() + Globals.SongIncDecInterval);
            ((SeekBar)view.findViewById(R.id.pa_sbEndTime)).setProgress(Song.get().getEndTime());
        });

        view.findViewById(R.id.pa_btnPause).setOnClickListener(v -> {
            if(Song.get().isPlaying())
                Song.get().pause();
            else
                Song.get().unpause();
        });

        view.findViewById(R.id.pa_btnSave).setOnClickListener(this::onLoopSaveClicked);

        view.findViewById(R.id.pa_lblStartTime).setOnClickListener(v -> {
            Song.get().setStartTime(((SeekBar)view.findViewById(R.id.pa_sbProgress)).getProgress());
            ((SeekBar)view.findViewById(R.id.pa_sbStartTime)).setProgress(Song.get().getStartTime());
        });

        view.findViewById(R.id.pa_lblEndTime).setOnClickListener(v -> {
            Song.get().setEndTime(((SeekBar)view.findViewById(R.id.pa_sbProgress)).getProgress());
            ((SeekBar)view.findViewById(R.id.pa_sbEndTime)).setProgress(Song.get().getEndTime());
        });

        txtInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                Globals.SongIncDecInterval = !s.toString().isEmpty() ? Integer.parseInt(s.toString()) : 500;
                try {
                    Globals.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //endregion

        // Set progresses of seekbars so that the listeners get activated and the labels are updated
        sbStartTime.setProgress(1);
        sbStartTime.setProgress(Song.get().getStartTime());
        sbEndTime.setProgress(Song.get().getEndTime());
    }

    private void onLoopSaveClicked(View view) {
        LayoutInflater inflater = (LayoutInflater)
                getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.save_dialog, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });

        popupView.findViewById(R.id.sd_btnSave).setOnClickListener(v -> {
            String name = ((EditText)popupView.findViewById(R.id.sd_txtSave)).getText().toString();
            if(name.isEmpty() || name.contains("_"))
                return;

            popupWindow.dismiss();

            try {
                Loop.save(Song.get(), Loop.toFile(Song.get().getName(), name));
                Globals.loadAvailableLoops();
            } catch(IOException e) {
                Utils.messageBox(getContext(), "Failed to save loop " + name, e.getMessage());
            }
        });
    }

    public void unload() {
        Song.get().pause();
        Song.get().seekTo(0);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
