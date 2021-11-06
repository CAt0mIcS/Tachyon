package com.de.mucify.utils;

import android.content.ContextWrapper;
import android.os.Environment;

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

    public final static Comparator<Song> AlphabeticalSongComparator = Comparator.comparing(Song::getTitle);

    public static void load(ContextWrapper context) {
        DataDirectory = new File(context.getDataDir().getPath() + "/files");
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        loadAvailableSongs();
        loadAvailableLoops();
        loadAvailablePlaylists();
    }

    public static void loadAvailableSongs() {

    }

    public static void loadAvailableLoops() {

    }

    public static void loadAvailablePlaylists() {

    }
}
