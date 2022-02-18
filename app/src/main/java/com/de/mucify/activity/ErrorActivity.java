package com.de.mucify.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.MucifyApplication;
import com.de.mucify.R;
import com.de.mucify.activity.controller.MultiAudioSelectController;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_activity);
        MucifyApplication.setCurrentActivity(this);

        EditText txtError = findViewById(R.id.txtError);
        txtError.setText("An exception occured. Please restart the app. If it continues to happen, send the text below to one of the developers. Thank you for your patience.\n\n" + getIntent().getStringExtra("Error"));
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
