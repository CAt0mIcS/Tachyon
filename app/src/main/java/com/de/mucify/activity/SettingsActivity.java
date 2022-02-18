package com.de.mucify.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.SingleAudioSelectController;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.PermissionManager;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MucifyApplication.setCurrentActivity(this);

        setContentView(R.layout.settings_activity);

        BottomNavigationView btmNav = findViewById(R.id.btmNav);

        // If the playlist switches back to this activity, we need to know if we want the Song
        // or Loop menu item selected, default will be Song item
        btmNav.setSelectedItemId(R.id.settings);
        btmNav.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.songs:
                case R.id.loops:
                    Intent i = new Intent(SettingsActivity.this, SingleAudioActivity.class);
                    i.putExtra("NavItemID", item.getItemId());
                    startActivity(i);
                    finish();
                    break;
                case R.id.playlists:
                    Intent i2 = new Intent(SettingsActivity.this, MultiAudioActivity.class);
                    startActivity(i2);
                    finish();
                    break;
                case R.id.settings:
                    return false;
            }

            return true;
        });

        EditText txtAudioUpdateInterval = findViewById(R.id.s_editAudioUpdateInterval);
        EditText txtSongIncDecInterval = findViewById(R.id.s_editSongIncDecInterval);
        SwitchCompat randomizePlaylistOrder = findViewById(R.id.s_swRandomizePlaylistOrder);
        SwitchCompat useAudioFocus = findViewById(R.id.s_swUseAudioFocus);

        txtAudioUpdateInterval.setText(String.valueOf(UserSettings.AudioUpdateInterval));
        txtSongIncDecInterval.setText(String.valueOf(UserSettings.SongIncDecInterval));
        randomizePlaylistOrder.setChecked(UserSettings.RandomizePlaylistSongOrder);
        useAudioFocus.setChecked(!UserSettings.UseAudioFocus);

        txtAudioUpdateInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                int newInterval = Integer.parseInt(editable.toString());
                if(newInterval > 0) {
                    UserSettings.AudioUpdateInterval = newInterval;
                    saveSettings();
                }
            }
        });

        txtSongIncDecInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                int newInterval = Integer.parseInt(editable.toString());
                if(newInterval > 0) {
                    UserSettings.SongIncDecInterval = newInterval;
                    saveSettings();
                }
            }
        });

        randomizePlaylistOrder.setOnCheckedChangeListener((compoundButton, b) -> {
            UserSettings.RandomizePlaylistSongOrder = b;
            saveSettings();
        });

        useAudioFocus.setOnCheckedChangeListener((compoundButton, b) -> {
            UserSettings.UseAudioFocus = !b;
            saveSettings();
        });
    }

    private void saveSettings(){
        try {
            UserSettings.save();
        } catch (IOException e) {
            Utils.startErrorActivity("Failed to save user settings\n" + Utils.getDetailedError(e));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MucifyApplication.activityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MucifyApplication.activityPaused(this);
    }
}
