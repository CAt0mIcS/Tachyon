package com.de.mucify.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.R;
import com.de.mucify.UserData;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;


public class ActivitySettings extends AppCompatActivity {
    SwitchMaterial mSwitchAudioFocus;
    TextInputEditText mAudioIncDecInterval;
    TextInputEditText mAudioUpdateInterval;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mSwitchAudioFocus = findViewById(R.id.switchAudioFocus);
        mSwitchAudioFocus.setOnClickListener(v -> {
            UserData.IgnoreAudioFocus = !mSwitchAudioFocus.isChecked();
        });

        mAudioIncDecInterval = findViewById(R.id.editAudioIncDecInterval);
        mAudioUpdateInterval = findViewById(R.id.editAudioUpdateInterval);

        updateUI();

        mAudioIncDecInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int interval = Integer.parseInt(s.toString());
                    if(interval == 0)
                        throw new NumberFormatException();
                    UserData.SongIncDecInterval = interval;
                    mAudioIncDecInterval.setError(null);
                } catch (NumberFormatException ignored) {
                    mAudioIncDecInterval.setError(getString(R.string.error_number_invalid));
                }
            }
        });

        mAudioUpdateInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int interval = Integer.parseInt(s.toString());
                    if(interval == 0)
                        throw new NumberFormatException();
                    UserData.AudioUpdateInterval = interval;
                    mAudioUpdateInterval.setError(null);
                } catch (NumberFormatException ignored) {
                    mAudioUpdateInterval.setError(getString(R.string.error_number_invalid));
                }
            }
        });

        findViewById(R.id.action_reset_settings).setOnClickListener(v -> displayWarningDialog());

        MaterialToolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserData.save();
    }

    private void displayWarningDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.dialog_reset_message)
                .setPositiveButton(R.string.reset_settings, (dialog, id) -> {
                    UserData.reset();
                    updateUI();
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> { })
                .create().show();
    }

    private void updateUI() {
        // MY_TODO: Figure out if we even need to synchronize reading (other thread might be writing at this exact moment?)
        mSwitchAudioFocus.setChecked(!UserData.IgnoreAudioFocus);
        mAudioIncDecInterval.setText(String.valueOf(UserData.SongIncDecInterval));
        mAudioUpdateInterval.setText(String.valueOf(UserData.AudioUpdateInterval));
    }
}
