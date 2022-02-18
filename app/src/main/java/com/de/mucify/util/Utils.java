package com.de.mucify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.de.mucify.MucifyApplication;
import com.de.mucify.activity.ErrorActivity;
import com.de.mucify.activity.SettingsActivity;
import com.de.mucify.playable.Song;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class Utils {
    public static String millisecondsToReadableString(int progress) {
        long millis = progress % 1000;
        long second = (progress / 1000) % 60;
        long minute = (progress / (1000 * 60)) % 60;
        long hour = (progress / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }

    public static boolean isDarkModeEnabled(Context context) {
        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void enableDarkMode() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static void startErrorActivity(String error) {
        Intent i = new Intent(MucifyApplication.getCurrentActivity(), ErrorActivity.class);
        i.putExtra("Error", error);
        MucifyApplication.getCurrentActivity().startActivity(i);
        MucifyApplication.getCurrentActivity().finish();
    }

    public static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String getDetailedError(Exception e) {
        return e.getMessage() + "\n\n" + getStackTrace(e);
    }
}
