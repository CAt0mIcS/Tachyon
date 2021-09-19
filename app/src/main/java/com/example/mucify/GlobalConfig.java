package com.example.mucify;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

public class GlobalConfig {
    public static File DataDirectory;
    public static File MusicDirectory;

    public static boolean RandomizePlaylistSongOrder;
    public static int SongIncDecInterval;

    public final static String LoopFileExtension = ".loop";
    public final static String LoopFileIdentifier = "LOOP_";

    public final static String PlaylistFileExtension = ".playlist";
    public final static String PlaylistFileIdentifier = "PLAYLIST_";

    public final static List<String> SupportedAudioExtensions = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".amr", ".flac", ".ota", ".imy", ".mp3", ".mkv", ".ogg", ".wav");

    public static File SettingsFile = null;

    public static void load(ContextWrapper context) throws IOException {
        DataDirectory = new File(context.getDataDir().getPath() + "/files");
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
        SettingsFile = new File(DataDirectory.getPath() + "/Settings.settings");

        // Default values in case of read failure
        RandomizePlaylistSongOrder = false;
        SongIncDecInterval = 500;

        // Set default values if the settings file doesn't exist
        if(!SettingsFile.exists()) {
            SettingsFile.createNewFile();
        }
        else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(SettingsFile));
                RandomizePlaylistSongOrder = Boolean.parseBoolean(reader.readLine());
                SongIncDecInterval = Integer.parseInt(reader.readLine());
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();

                // Save file if something went wrong
                save();
            }
        }
    }

    public static void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SettingsFile));
        writer.write(String.valueOf(RandomizePlaylistSongOrder) + '\n');
        writer.write(String.valueOf(SongIncDecInterval) + '\n');
        writer.close();
    }
}
