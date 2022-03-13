package com.de.mucify;

import android.content.ContextWrapper;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.provider.ContactsContract;

import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private final static Comparator<Song> mSongComparator = new Comparator<Song>() {
        @Override
        public int compare(Song o1, Song o2) {
            return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }
    };
    private final static Comparator<Playlist> mPlaylistComparator = new Comparator<Playlist>() {
        @Override
        public int compare(Playlist o1, Playlist o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    public static void load(ContextWrapper context) {
        DataDirectory = context.getFilesDir();
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        if(!DataDirectory.exists())
            DataDirectory.mkdirs();
    }

    public static ArrayList<Song> loadAvailableSongs() {
        AvailableSongs.clear();
        loadFiles(MusicDirectory, true, false, false);
        Collections.sort(AvailableSongs, mSongComparator);
        return AvailableSongs;
    }

    public static ArrayList<Song> loadAvailableLoops() {
        AvailableLoops.clear();
        loadFiles(DataDirectory, false, true, false);
        Collections.sort(AvailableLoops, mSongComparator);
        return AvailableLoops;
    }

    public static ArrayList<Playlist> loadAvailablePlaylists() {
        AvailablePlaylists.clear();
        loadFiles(DataDirectory, false, false, true);
        Collections.sort(AvailablePlaylists, mPlaylistComparator);
        return AvailablePlaylists;
    }

    public static int getSongIndex(Song song) {
        for(int i = 0; i < AvailableSongs.size(); ++i) {
            if(AvailableSongs.get(i).equalsUninitialized(song))
                return i;
        }
        return -1;
    }

    public static int getLoopIndex(Song song) {
        for(int i = 0; i < AvailableLoops.size(); ++i) {
            if(AvailableLoops.get(i).equalsUninitialized(song))
                return i;
        }
        return -1;
    }

    public static int getPlaylistIndex(Playlist playlist) {
        for(int i = 0; i < AvailablePlaylists.size(); ++i) {
            if(AvailablePlaylists.get(i).equalsUninitialized(playlist))
                return i;
        }
        return -1;
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
//                                    Utils.startErrorActivity("Failed to load song: " + file + "\n" + Utils.getDetailedError(e));
                                }
                            }
                        }
                        else if(loop) {
                            String extension = FileManager.getFileExtension(file.getName());
                            if(extension.equals(LoopFileExtension) && file.getName().indexOf(LoopFileIdentifier) == 0) {
                                try {
                                    AvailableLoops.add(new Song(file));
                                } catch (Song.LoadingFailedException e) {
//                                    Utils.startErrorActivity("Failed to load loop: " + file + "\n" + Utils.getDetailedError(e));
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