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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

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

    public final static ArrayList<File> AvailableSongs = new ArrayList<>();
    public final static ArrayList<File> AvailableLoops = new ArrayList<>();
    public final static ArrayList<File> AvailablePlaylists = new ArrayList<>();


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

        loadAvailableSongs(GlobalConfig.MusicDirectory);
        loadAvailableLoops(GlobalConfig.DataDirectory);

        Comparator<File> comparator = Comparator.comparing(File::getName);
        AvailableSongs.sort(comparator);
        AvailableLoops.sort(comparator);
    }

    public static void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(SettingsFile));
        writer.write(String.valueOf(RandomizePlaylistSongOrder) + '\n');
        writer.write(String.valueOf(SongIncDecInterval) + '\n');
        writer.close();
    }

    public static void loadAvailablePlaylists(File dir) {
        loadFiles(dir, false, false, true);
    }

    public static void loadAvailableSongs(File dir) {
        loadFiles(dir, true, false, false);
    }

    public static void loadAvailableLoops(File dir) {
        loadFiles(dir, false, true, false);
    }


    private static void loadFiles(File dir, boolean song, boolean loop, boolean playlist) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadFiles(file, song, loop, playlist);
                    } else {
                        if(song) {
                            Optional<String> extension = Util.getFileExtension(file.getName());
                            if(extension.isPresent() && GlobalConfig.SupportedAudioExtensions.contains(extension.get()))
                                AvailableSongs.add(file);
                        }
                        else if(loop) {
                            Optional<String> extension = Util.getFileExtension(file.getName());
                            if(extension.isPresent() && extension.get().equals(GlobalConfig.LoopFileExtension) && file.getName().indexOf(GlobalConfig.LoopFileIdentifier) == 0)
                                AvailableLoops.add(file);
                        }
                        else if(playlist) {
                            Optional<String> extension = Util.getFileExtension(file.getName());
                            if(extension.isPresent() && extension.get().equals(GlobalConfig.PlaylistFileExtension) && file.getName().indexOf(GlobalConfig.PlaylistFileIdentifier) == 0)
                                AvailablePlaylists.add(file);
                        }
                    }
                }
            }
        }
    }
}
