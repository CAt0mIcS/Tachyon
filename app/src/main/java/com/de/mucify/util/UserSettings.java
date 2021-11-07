package com.de.mucify.util;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedWriter;
import java.io.File;
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

    public static void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SettingsFile));
        writer.write(String.valueOf(RandomizePlaylistSongOrder) + '\n');
        writer.write(String.valueOf(SongIncDecInterval) + '\n');
        writer.close();
    }
}
