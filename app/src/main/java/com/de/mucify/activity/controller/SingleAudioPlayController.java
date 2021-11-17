package com.de.mucify.activity.controller;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Playlist;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;


public class SingleAudioPlayController {
    private final SingleAudioPlayActivity mActivity;

    private final Handler mHandler = new Handler();
    private final SeekBar mSbProgress;
    private final SeekBar mSbStartTime;
    private final SeekBar mSbEndTime;
    private final TextView mLblProgress;
    private final TextView mLblStartTime;
    private final TextView mLblEndTime;
    private final ImageButton mBtnPlayPause;

    private boolean mIsSeeking = false;

    public SingleAudioPlayController(SingleAudioPlayActivity activity) {
        mActivity = activity;

        TelephonyManager mgr = (TelephonyManager)mActivity.getSystemService(Context.TELEPHONY_SERVICE);
        if(mgr != null) {
            // Incoming call || A call is dialing, active or on hold
            mgr.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    // Incoming call || A call is dialing, active or on hold
                    if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        if (!AudioController.get().isSongNull())
                            AudioController.get().pauseSong();
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mSbProgress = mActivity.findViewById(R.id.pa_sbProgress);
        mSbStartTime = mActivity.findViewById(R.id.pa_sbStartTime);
        mSbEndTime = mActivity.findViewById(R.id.pa_sbEndTime);
        mLblProgress = mActivity.findViewById(R.id.pa_lblProgress);
        mLblStartTime = mActivity.findViewById(R.id.pa_lblStartTime);
        mLblEndTime = mActivity.findViewById(R.id.pa_lblEndTime);
        mBtnPlayPause = mActivity.findViewById(R.id.pa_btnPause);


        int duration = AudioController.get().getSongDuration() / UserSettings.AudioUpdateInterval;
        mSbProgress.setMax(duration);
        mSbStartTime.setMax(duration);
        mSbEndTime.setMax(duration);

        ((TextView)mActivity.findViewById(R.id.pa_lblSongName)).setText(AudioController.get().getSongTitle() + " " + mActivity.getString(R.string.by) + " " + AudioController.get().getSongArtist());

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
        mSbStartTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int startTime = seekBar.getProgress() * UserSettings.AudioUpdateInterval;
                AudioController.get().setSongStartTime(startTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLblStartTime.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });
        mSbEndTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = seekBar.getProgress() * UserSettings.AudioUpdateInterval;
                AudioController.get().setSongEndTime(endTime);
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mLblEndTime.setText(Utils.millisecondsToReadableString(progress * UserSettings.AudioUpdateInterval));
            }
        });
        mBtnPlayPause.setOnClickListener(v -> {
            if(AudioController.get().isSongPlaying())
                AudioController.get().pauseSong();
            else
                AudioController.get().unpauseSong();
        });
        AudioController.get().addOnSongPausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_black_play), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongUnpausedListener(song -> mBtnPlayPause.setImageResource(R.drawable.ic_black_pause), AudioController.INDEX_DONT_CARE);
        AudioController.get().addOnSongResetListener(song -> {
            if(MucifyApplication.isActivityVisible()) {
                Intent i = new Intent(mActivity, SingleAudioActivity.class);
                i.putExtra("NavItemID", mActivity.getNavItemID());
                mActivity.startActivity(i);
                mActivity.finish();
            }
        }, AudioController.INDEX_DONT_CARE);
        MucifyApplication.addOnActivityVisibilityChangedListener((act, becameVisible) -> {
            if(becameVisible && AudioController.get().isSongNull() && act instanceof SingleAudioPlayActivity) {
                Intent i = new Intent(mActivity, SingleAudioActivity.class);
                i.putExtra("NavItemID", mActivity.getNavItemID());
                mActivity.startActivity(i);
                mActivity.finish();
            }
        });
        mActivity.findViewById(R.id.pa_btnStartTimeDec).setOnClickListener(v -> {
            AudioController.get().setSongStartTime(AudioController.get().getSongStartTime() - UserSettings.SongIncDecInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnStartTimeInc).setOnClickListener(v -> {
            AudioController.get().setSongStartTime(AudioController.get().getSongStartTime() + UserSettings.SongIncDecInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnEndTimeDec).setOnClickListener(v -> {
            AudioController.get().setSongEndTime(AudioController.get().getSongEndTime() - UserSettings.SongIncDecInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnEndTimeInc).setOnClickListener(v -> {
            AudioController.get().setSongEndTime(AudioController.get().getSongEndTime() + UserSettings.SongIncDecInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mLblStartTime.setOnClickListener(v -> {
            AudioController.get().setSongStartTime(mSbProgress.getProgress() * UserSettings.AudioUpdateInterval);
            mSbStartTime.setProgress(AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval);
        });
        mLblEndTime.setOnClickListener(v -> {
            AudioController.get().setSongEndTime(mSbProgress.getProgress() * UserSettings.AudioUpdateInterval);
            mSbEndTime.setProgress(AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval);
        });
        mActivity.findViewById(R.id.pa_btnSave).setOnClickListener(this::onLoopSave);
        ((TextView)mActivity.findViewById(R.id.pa_txtInterval)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                UserSettings.SongIncDecInterval = !s.toString().isEmpty() ? Integer.parseInt(s.toString()) : 500;
                try {
                    UserSettings.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    Snackbar.make(mActivity.findViewById(R.id.pa_scrollview), "Unable to save settings: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        mActivity.findViewById(R.id.pa_btnAddToPlaylist).setOnClickListener(this::onAddToPlaylist);

        // Perform click to update image
        mBtnPlayPause.performClick();
        mBtnPlayPause.performClick();

        // Call listeners to set label text
        mSbStartTime.setProgress(1);
        mSbStartTime.setProgress(AudioController.get().getSongStartTime() != 0 ? AudioController.get().getSongStartTime() / UserSettings.AudioUpdateInterval : 0);
        mSbEndTime.setProgress(AudioController.get().getSongEndTime() != 0 ? AudioController.get().getSongEndTime() / UserSettings.AudioUpdateInterval : 0);
    }

    private void onLoopSave(View v) {
        new AlertDialog.Builder(mActivity)
                .setMessage(R.string.dialog_loop_name)
                .setView(mActivity.getLayoutInflater().inflate(R.layout.save_loop_new_playlist_alert_dialog_layout, null))
                .setPositiveButton(R.string.save, (dialog, id) -> {

                    String loopName = ((EditText)((AlertDialog)dialog).findViewById(R.id.dialog_txtName)).getText().toString();
                    if(loopName.isEmpty() || loopName.contains("_")) {
                        Toast.makeText(mActivity, "Failed to save loop: Name mustn't contain '_' or be empty", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        AudioController.get().saveAsLoop(loopName);
                    } catch (IOException e) {
                        Toast.makeText(mActivity, "Failed to save loop: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    MediaLibrary.loadAvailableLoops();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    private void onAddToPlaylist(View v) {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.select_dialog_multichoice);
        MediaLibrary.loadAvailablePlaylists();

        ArrayList<String> checkedItems = new ArrayList<>();

        for(Playlist playlist : MediaLibrary.AvailablePlaylists)
            arrayAdapter.add(playlist.getName());

        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.add_to_playlist))
                .setPositiveButton(mActivity.getString(R.string.ok), (dial, id) -> {
                    for(int i = 0; i < arrayAdapter.getCount(); ++i) {
                        if(checkedItems.contains(arrayAdapter.getItem(i))) {
                            Playlist playlist = MediaLibrary.AvailablePlaylists.get(i);
                            playlist.create(mActivity);
                            playlist.addSong(AudioController.get().getSong());
                            try {
                                playlist.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(mActivity, "Error saving playlist: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                })
                .setNegativeButton(mActivity.getString(R.string.cancel), (dial, i) -> dial.dismiss())
                .setAdapter(arrayAdapter, null)
                .create();

        dialog.getListView().setItemsCanFocus(false);
        dialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        dialog.getListView().setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView textView = (CheckedTextView)view;
            if(textView.isChecked())
                checkedItems.add(MediaLibrary.AvailablePlaylists.get(position).getName());
            else
                checkedItems.remove(MediaLibrary.AvailablePlaylists.get(position).getName());
        });
        dialog.show();
    }
}
