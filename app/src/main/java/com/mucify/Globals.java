package com.mucify;

import android.content.ContextWrapper;
import android.os.Environment;

import com.mucify.objects.Loop;
import com.mucify.objects.Playlist;
import com.mucify.objects.Song;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Globals {
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

    public final static ArrayList<String> AvailableSongNames = new ArrayList<>();
    public final static ArrayList<String> AvailableLoopNames = new ArrayList<>();
    public final static ArrayList<String> AvailablePlaylistNames = new ArrayList<>();

    public static void load(ContextWrapper context) throws IOException {
        DataDirectory = new File(context.getDataDir().getPath() + "/files");
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
        SettingsFile = new File(DataDirectory.getPath() + "/Settings.settings");

        // Default values in case of read failure
        RandomizePlaylistSongOrder = false;
        SongIncDecInterval = 500;

        loadAvailableSongs();
        loadAvailableLoops();
        loadAvailablePlaylists();

        // Sort songs and loops alphabetically
        Comparator<File> comparator = Comparator.comparing(File::getName);
        AvailableSongs.sort(comparator);
        AvailableLoops.sort(comparator);

        if(!DataDirectory.exists())
            DataDirectory.mkdir();
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

    public static void loadAvailablePlaylists() {
        AvailablePlaylists.clear();
        AvailablePlaylistNames.clear();
        loadFiles(DataDirectory, false, false, true);
    }

    public static void loadAvailableSongs() {
        AvailableSongs.clear();
        AvailableSongNames.clear();
        loadFiles(MusicDirectory, true, false, false);
    }

    public static void loadAvailableLoops() {
        AvailableLoops.clear();
        AvailableLoopNames.clear();
        loadFiles(DataDirectory, false, true, false);
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
                            String extension = Utils.getFileExtension(file.getName());
                            if(SupportedAudioExtensions.contains(extension)) {
                                AvailableSongs.add(file);
                                AvailableSongNames.add(Song.toName(file));
                            }
                        }
                        else if(loop) {
                            String extension = Utils.getFileExtension(file.getName());
                            if(extension.equals(LoopFileExtension) && file.getName().indexOf(LoopFileIdentifier) == 0) {
                                AvailableLoops.add(file);
                                AvailableLoopNames.add(Loop.toName(file));
                            }
                        }
                        else if(playlist) {
                            String extension = Utils.getFileExtension(file.getName());
                            if(extension.equals(PlaylistFileExtension) && file.getName().indexOf(PlaylistFileIdentifier) == 0) {
                                AvailablePlaylists.add(file);
                                AvailablePlaylistNames.add(Playlist.toName(file));
                            }
                        }
                    }
                }
            }
        }
    }
}
