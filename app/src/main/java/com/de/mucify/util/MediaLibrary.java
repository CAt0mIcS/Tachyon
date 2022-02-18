package com.de.mucify.util;

import android.content.ContextWrapper;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.provider.ContactsContract;

import com.de.mucify.playable.Playlist;
import com.de.mucify.playable.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MediaLibrary {
    public static File DataDirectory;
    public static File MusicDirectory;

    public final static String LoopFileExtension = ".loop";
    public final static String LoopFileIdentifier = "LOOP_";

    public final static String PlaylistFileExtension = ".playlist";
    public final static String PlaylistFileIdentifier = "PLAYLIST_";

    public final static List<String> SupportedAudioExtensions = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".amr", ".flac", ".ota", ".imy", ".mp3", ".mkv", ".ogg", ".wav");

    public final static ArrayList<Song> AvailableSongs = new ArrayList<>();
    public final static ArrayList<Song> AvailableLoops = new ArrayList<>();
    public final static ArrayList<Playlist> AvailablePlaylists = new ArrayList<>();

    public static void load(ContextWrapper context) {
        DataDirectory = new File(context.getDataDir().getPath() + "/files");
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        if(!DataDirectory.exists())
            DataDirectory.mkdirs();
    }

    public static ArrayList<Song> loadAvailableSongs() {
        AvailableSongs.clear();
        loadFiles(MusicDirectory, true, false, false);
        AvailableSongs.sort(Comparator.comparing(Song::getTitle));
        return AvailableSongs;
    }

    public static ArrayList<Song> loadAvailableLoops() {
        AvailableLoops.clear();
        loadFiles(DataDirectory, false, true, false);
        AvailableLoops.sort(Comparator.comparing(Song::getTitle));
        return AvailableLoops;
    }

    public static ArrayList<Playlist> loadAvailablePlaylists() {
        AvailablePlaylists.clear();
        loadFiles(DataDirectory, false, false, true);
        AvailablePlaylists.sort(Comparator.comparing(Playlist::getName));
        return AvailablePlaylists;
    }


    private static void loadFiles(File dir, boolean song, boolean loop, boolean playlist) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {

                for (File file : files) {
                    if (file.isDirectory()) {
                        loadFiles(file, song, loop, playlist);
                    } else {
                        if(song) {
                            String extension = FileManager.getFileExtension(file.getName());
                            if(SupportedAudioExtensions.contains(extension)) {
                                try {
                                    AvailableSongs.add(new Song(file));
                                } catch (Song.LoadingFailedException e) {
                                    Utils.startErrorActivity("Failed to load song: " + file + "\n" + Utils.getDetailedError(e));
                                }
                            }
                        }
                        else if(loop) {
                            String extension = FileManager.getFileExtension(file.getName());
                            if(extension.equals(LoopFileExtension) && file.getName().indexOf(LoopFileIdentifier) == 0) {
                                try {
                                    AvailableLoops.add(new Song(file));
                                } catch (Song.LoadingFailedException e) {
                                    Utils.startErrorActivity("Failed to load loop: " + file + "\n" + Utils.getDetailedError(e));
                                }
                            }
                        }
                        else if(playlist) {
                            String extension = FileManager.getFileExtension(file.getName());
                            if(extension.equals(PlaylistFileExtension) && file.getName().indexOf(PlaylistFileIdentifier) == 0) {
                                AvailablePlaylists.add(new Playlist(file));
                            }
                        }
                    }
                }
            }
        }
    }
}
