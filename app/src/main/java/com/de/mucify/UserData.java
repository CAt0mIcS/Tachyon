package com.de.mucify;

import android.content.ContextWrapper;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserData {
    public static File mSettingsFile;

    // Keep playing even if audio focus is lost
    public static boolean IgnoreAudioFocus = false;

    // Interval by which the seekbars in the player should increment/decrement the time in milliseconds
    public static int SongIncDecInterval = 100;
    // Interval by which the loop/song done check will be run
    public static int AudioUpdateInterval = 100;

    public static File LastPlayedPlayback;
    public static int LastPlayedPlaybackPos = 0;

    public static void load(ContextWrapper context) {
        mSettingsFile = new File(context.getFilesDir().getAbsolutePath() + "/Settings.txt");

        String jsonString;
        // If reading fails, save default settings
        try {
            StringBuilder jsonBuilder = new StringBuilder();

            BufferedReader reader = new BufferedReader(new FileReader(mSettingsFile));
            while(reader.ready()) {
                jsonBuilder.append(reader.readLine()).append('\n');
            }
            reader.close();

            jsonString = jsonBuilder.toString();
        } catch (IOException e) {
            save();
            return;
        }

        // If reading fails, save default settings
        try {
            JSONObject json = new JSONObject(jsonString);
            IgnoreAudioFocus = json.optBoolean("IgnoreAudioFocus", IgnoreAudioFocus);
            SongIncDecInterval = json.optInt("SongIncDecInterval", SongIncDecInterval);
            AudioUpdateInterval = json.optInt("AudioUpdateInterval", AudioUpdateInterval);
            LastPlayedPlaybackPos = json.optInt("LastPlayedPlaybackPos", LastPlayedPlaybackPos);

            if(json.has("LastPlayedPlayback"))
                LastPlayedPlayback = new File(json.getString("LastPlayedPlayback"));
        } catch (JSONException e) {
            save();
            return;
        }
    }

    public static void save() {
        Map<String, String> map = new HashMap<>();
        map.put("IgnoreAudioFocus", String.valueOf(IgnoreAudioFocus));
        map.put("SongIncDecInterval", String.valueOf(SongIncDecInterval));
        map.put("AudioUpdateInterval", String.valueOf(AudioUpdateInterval));
        if(LastPlayedPlayback != null) {
            map.put("LastPlayedPlayback", LastPlayedPlayback.getAbsolutePath());
            map.put("LastPlayedPlaybackPos", String.valueOf(LastPlayedPlaybackPos));
        }


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(mSettingsFile));
            writer.write(new JSONObject(map).toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
