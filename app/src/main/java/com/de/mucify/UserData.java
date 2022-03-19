package com.de.mucify;

import android.content.ContextWrapper;


import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserData {
    public static final Object SettingsLock = new Object();

    public static File mSettingsFile;

    // Keep playing even if audio focus is lost
    public static boolean IgnoreAudioFocus = false;

    // Interval by which the seekbars in the player should increment/decrement the time in milliseconds
    public static int SongIncDecInterval = 100;
    // Interval by which the loop/song done check will be run
    public static int AudioUpdateInterval = 100;
    // Max number of playbacks stored in the history
    public static int MaxPlaybacksInHistory = 25;

    public static final ArrayList<PlaybackInfo> PlaybackInfos = new ArrayList<>();

    public static class PlaybackInfo {

        /**
         * Path to the last file which was opened. Could be path to playlist, loop or song file
         */
        public File PlaybackPath;

        /**
         * Is PlaybackPath is a playlist then this will be set to the last song/loop that was played
         * in the playlist
         */
        public File LastPlayedPlaybackInPlaylist;

        /**
         * Position in milliseconds of the song or loop
         */
        public int PlaybackPos = 0;

        public boolean isPlaylist() {
            return LastPlayedPlaybackInPlaylist != null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PlaybackInfo that = (PlaybackInfo) o;
            return PlaybackPath.equals(that.PlaybackPath) && Objects.equals(LastPlayedPlaybackInPlaylist, that.LastPlayedPlaybackInPlaylist);
        }
    }

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

            synchronized (SettingsLock) {
                IgnoreAudioFocus = json.optBoolean("IgnoreAudioFocus", IgnoreAudioFocus);
                SongIncDecInterval = json.optInt("SongIncDecInterval", SongIncDecInterval);
                AudioUpdateInterval = json.optInt("AudioUpdateInterval", AudioUpdateInterval);
                MaxPlaybacksInHistory = json.optInt("MaxPlaybacksInHistory", MaxPlaybacksInHistory);

                PlaybackInfos.clear();
                JSONArray playbackInfos = json.getJSONArray("PlaybackInfos");
                for(int i = 0; i < playbackInfos.length(); ++i) {
                    JSONObject obj = playbackInfos.getJSONObject(i);
                    PlaybackInfo info = new PlaybackInfo();

                    info.PlaybackPos = obj.optInt("PlaybackPos");
                    if(obj.has("PlaybackPath"))
                        info.PlaybackPath = new File(obj.getString("PlaybackPath"));
                    if(obj.has("LastPlayedPlaybackInPlaylist"))
                        info.LastPlayedPlaybackInPlaylist = new File(obj.getString("LastPlayedPlaybackInPlaylist"));
                    PlaybackInfos.add(info);
                }

                // Remove oldest playbacks if we exceed max playbacks in history
                if(PlaybackInfos.size() > MaxPlaybacksInHistory)
                    PlaybackInfos.subList(0, PlaybackInfos.size() - MaxPlaybacksInHistory).clear();
            }

        } catch (JSONException e) {
            save();
            return;
        }
    }

    public static void save() {
        Map<String, String> map = new HashMap<>();

        synchronized (SettingsLock) {
            map.put("IgnoreAudioFocus", String.valueOf(IgnoreAudioFocus));
            map.put("SongIncDecInterval", String.valueOf(SongIncDecInterval));
            map.put("AudioUpdateInterval", String.valueOf(AudioUpdateInterval));
            map.put("MaxPlaybacksInHistory", String.valueOf(MaxPlaybacksInHistory));
        }

        JSONObject json = new JSONObject(map);
        JSONArray array = new JSONArray();
        for(int i = 0; i < PlaybackInfos.size(); ++i) {
            JSONObject obj = new JSONObject();
            PlaybackInfo info = PlaybackInfos.get(i);
            try {
                // Only store playback position for previously played playback
                if(i == PlaybackInfos.size() - 1)
                    obj.put("PlaybackPos", info.PlaybackPos);
                obj.putOpt("PlaybackPath", info.PlaybackPath);
                obj.putOpt("LastPlayedPlaybackInPlaylist", info.LastPlayedPlaybackInPlaylist);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            array.put(obj);
        }
        try {
            json.put("PlaybackInfos", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(mSettingsFile));
            writer.write(json.toString(4));
            writer.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void addPlaybackInfo(@NonNull PlaybackInfo info) {
        for(int i = 0; i < PlaybackInfos.size(); ++i) {
            if(info.equals(PlaybackInfos.get(i))) {
                PlaybackInfos.remove(i);
                PlaybackInfos.add(info);
                return;
            }
        }
        synchronized (SettingsLock) {
            PlaybackInfos.add(info);
        }
    }

    public static void reset() {
        synchronized (SettingsLock) {
            IgnoreAudioFocus = false;
            SongIncDecInterval = 100;
            AudioUpdateInterval = 100;
            MaxPlaybacksInHistory = 25;
        }
        mSettingsFile.delete();
    }
}
