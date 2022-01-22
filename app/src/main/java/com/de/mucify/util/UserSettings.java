package com.de.mucify.util;

import com.de.mucify.playable.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UserSettings {
    public static final File SettingsFile = new File(MediaLibrary.DataDirectory.getPath() + "/Settings.settings");

    /**
     * Specifies when the runnable should update again
     * Runnable updates all labels displaying media player position and seekbars
     */
    public static int AudioUpdateInterval = 100;
    public static int SongIncDecInterval = 100;
    public static boolean RandomizePlaylistSongOrder = true;
    public static boolean UseAudioFocus = true;

    public static void load() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SettingsFile));
            int audioUpdate = Integer.parseInt(reader.readLine());
            int songDec = Integer.parseInt(reader.readLine());
            boolean randomize = Boolean.parseBoolean(reader.readLine());
            boolean audioFocus = Boolean.parseBoolean(reader.readLine());

            AudioUpdateInterval = audioUpdate;
            SongIncDecInterval = songDec;
            RandomizePlaylistSongOrder = randomize;
            UseAudioFocus = audioFocus;

            reader.close();
        } catch (Exception ignored) { }
    }

    public static void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SettingsFile));
        writer.write(String.valueOf(AudioUpdateInterval) + '\n');
        writer.write(String.valueOf(SongIncDecInterval) + '\n');
        writer.write(String.valueOf(RandomizePlaylistSongOrder) + '\n');
        writer.write(String.valueOf(UseAudioFocus) + '\n');
        writer.close();
    }
}
