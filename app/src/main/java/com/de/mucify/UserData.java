package com.de.mucify;

import android.content.Context;

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

public class UserData {
    private static final ArrayList<Callback> mCallbacks = new ArrayList<>();

    public static File mSettingsFile;

    // Keep playing even if audio focus is lost
    private static boolean mIgnoreAudioFocus = false;
    private static final Object mIgnoreAudioFocusLock = new Object();

    // Interval by which the seekbars in the player should increment/decrement the time in milliseconds
    private static int mSongIncDecInterval = 100;
    private static final Object mSongIncDecIntervalLock = new Object();

    // Interval by which the loop/song done check will be run
    private static int mAudioUpdateInterval = 100;
    private static final Object mAudioUpdateIntervalLock = new Object();

    // Max number of playbacks stored in the history
    private static int mMaxPlaybacksInHistory = 25;
    private static final Object mMaxPlaybacksInHistoryLock = new Object();

    private static final ArrayList<PlaybackInfo> mPlaybackInfos = new ArrayList<>();
    private static final Object mPlaybackInfosLock = new Object();

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
            return PlaybackPath.equals(that.PlaybackPath);
        }
    }

    public static boolean getIgnoreAudioFocus() {
        synchronized (mIgnoreAudioFocusLock) {
            return mIgnoreAudioFocus;
        }
    }

    public static int getSongIncDecInterval() {
        synchronized (mSongIncDecIntervalLock) {
            return mSongIncDecInterval;
        }
    }

    public static int getAudioUpdateInterval() {
        synchronized (mAudioUpdateIntervalLock) {
            return mAudioUpdateInterval;
        }
    }

    public static int getMaxPlaybacksInHistory() {
        synchronized (mMaxPlaybacksInHistoryLock) {
            return mMaxPlaybacksInHistory;
        }
    }

    public static int getPlaybackInfoSize() {
        synchronized (mPlaybackInfosLock) {
            return mPlaybackInfos.size();
        }
    }

    public static PlaybackInfo getPlaybackInfo(int index) {
        synchronized (mPlaybackInfosLock) {
            return mPlaybackInfos.get(index);
        }
    }

    public static void setIgnoreAudioFocus(boolean ignore) {
        synchronized (mIgnoreAudioFocusLock) {
            mIgnoreAudioFocus = ignore;
        }
    }

    public static void setSongIncDecInterval(int interval) {
        synchronized (mSongIncDecIntervalLock) {
            mSongIncDecInterval = interval;
        }
    }

    public static void setAudioUpdateInterval(int interval) {
        synchronized (mAudioUpdateIntervalLock) {
            mAudioUpdateInterval = interval;
        }
    }

    public static void setMaxPlaybacksInHistory(int max) {
        synchronized (mMaxPlaybacksInHistoryLock) {
            mMaxPlaybacksInHistory = max;
        }
    }


    public static void load(Context context) {
        mSettingsFile = new File(context.getFilesDir().getAbsolutePath() + "/Settings.txt");

        String jsonString;
        // If reading fails, save default settings
        try {
            StringBuilder jsonBuilder = new StringBuilder();

            BufferedReader reader = new BufferedReader(new FileReader(mSettingsFile));
            while (reader.ready()) {
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

            setIgnoreAudioFocus(json.optBoolean("IgnoreAudioFocus", mIgnoreAudioFocus));
            setSongIncDecInterval(json.optInt("SongIncDecInterval", mSongIncDecInterval));
            setAudioUpdateInterval(json.optInt("AudioUpdateInterval", mAudioUpdateInterval));
            setMaxPlaybacksInHistory(json.optInt("MaxPlaybacksInHistory", mMaxPlaybacksInHistory));

            synchronized (mPlaybackInfosLock) {
                mPlaybackInfos.clear();
            }

            JSONArray playbackInfos = json.getJSONArray("PlaybackInfos");
            for (int i = 0; i < playbackInfos.length(); ++i) {
                JSONObject obj = playbackInfos.getJSONObject(i);
                PlaybackInfo info = new PlaybackInfo();

                info.PlaybackPos = obj.optInt("PlaybackPos");
                if (obj.has("PlaybackPath"))
                    info.PlaybackPath = new File(obj.getString("PlaybackPath"));
                if (obj.has("LastPlayedPlaybackInPlaylist"))
                    info.LastPlayedPlaybackInPlaylist = new File(obj.getString("LastPlayedPlaybackInPlaylist"));

                addPlaybackInfo(info);
            }

            // Remove oldest playbacks if we exceed max playbacks in history
            synchronized (mPlaybackInfosLock) {
                synchronized (mMaxPlaybacksInHistoryLock) {
                    if (mPlaybackInfos.size() > mMaxPlaybacksInHistory)
                        mPlaybackInfos.subList(0, mPlaybackInfos.size() - mMaxPlaybacksInHistory).clear();
                }
            }

        } catch (JSONException e) {
            save();
            return;
        }
    }

    public static void save() {
        Map<String, String> map = new HashMap<>();

        map.put("IgnoreAudioFocus", String.valueOf(getIgnoreAudioFocus()));
        map.put("SongIncDecInterval", String.valueOf(getSongIncDecInterval()));
        map.put("AudioUpdateInterval", String.valueOf(getAudioUpdateInterval()));
        map.put("MaxPlaybacksInHistory", String.valueOf(getMaxPlaybacksInHistory()));

        JSONObject json = new JSONObject(map);
        JSONArray array = new JSONArray();

        synchronized (mPlaybackInfosLock) {
            for (int i = 0; i < mPlaybackInfos.size(); ++i) {
                JSONObject obj = new JSONObject();
                PlaybackInfo info = mPlaybackInfos.get(i);
                try {
                    // Only store playback position for previously played playback
                    if (i == mPlaybackInfos.size() - 1)
                        obj.put("PlaybackPos", info.PlaybackPos);
                    obj.putOpt("PlaybackPath", info.PlaybackPath);
                    obj.putOpt("LastPlayedPlaybackInPlaylist", info.LastPlayedPlaybackInPlaylist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                array.put(obj);
            }
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
        synchronized (mPlaybackInfosLock) {
            for (int i = 0; i < mPlaybackInfos.size(); ++i) {
                if (info.equals(mPlaybackInfos.get(i))) {
                    mPlaybackInfos.remove(i);
                    mPlaybackInfos.add(info);
                    for (Callback c : mCallbacks)
                        c.onPlaybackInfoChanged();

                    return;
                }
            }
            mPlaybackInfos.add(info);
        }

        for (Callback c : mCallbacks)
            c.onPlaybackInfoChanged();
    }

    public static void reset() {
        setIgnoreAudioFocus(false);
        setSongIncDecInterval(100);
        setAudioUpdateInterval(100);
        setMaxPlaybacksInHistory(25);
        mSettingsFile.delete();
    }

    public static void addCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public static void removeCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public static class Callback {
        public void onPlaybackInfoChanged() {
        }
    }
}
