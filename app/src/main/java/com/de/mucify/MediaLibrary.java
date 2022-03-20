package com.de.mucify;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;
import com.de.mucify.service.MediaPlaybackService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MediaLibrary {
    public static File DataDirectory;
    public static File MusicDirectory;

    public static final String LoopFileExtension = ".loop";
    public static final String PlaylistFileExtension = ".playlist";

    public static final List<String> SupportedAudioExtensions = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".amr", ".flac", ".ota", ".imy", ".mp3", ".mkv", ".ogg", ".wav");

    public static final ArrayList<Song> AvailableSongs = new ArrayList<>();
    public static final ArrayList<Song> AvailableLoops = new ArrayList<>();
    public static final ArrayList<Playlist> AvailablePlaylists = new ArrayList<>();

    private static final Comparator<Song> mSongComparator = (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle());
    private static final Comparator<Playlist> mPlaylistComparator = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

    public static void load(Context context) {
        DataDirectory = context.getFilesDir();
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        if(!DataDirectory.exists())
            DataDirectory.mkdirs();
    }

    public static void loadAvailableSongs() {
        AvailableSongs.clear();
        loadFiles(MusicDirectory, true, false, false);
        Collections.sort(AvailableSongs, mSongComparator);
    }

    public static void loadAvailableLoops() {
        AvailableLoops.clear();
        loadFiles(DataDirectory, false, true, false);
        Collections.sort(AvailableLoops, mSongComparator);
    }

    public static void loadAvailablePlaylists() {
        AvailablePlaylists.clear();
        loadFiles(DataDirectory, false, false, true);
        Collections.sort(AvailablePlaylists, mPlaylistComparator);
    }

    public static boolean isSongMediaId(String mediaId) {
        return mediaId.contains("Song_");
    }
    public static boolean isLoopMediaId(String mediaId) {
        return mediaId.contains("Loop_");
    }
    public static boolean isPlaylistMediaId(String mediaId) {
        return mediaId.contains("Playlist_");
    }
    public static File getPathFromMediaId(String mediaId) {
        return new File(mediaId.substring(mediaId.indexOf('_') + 1));
    }

    public static Playback getPlaybackFromMediaId(String mediaId) {
        if (isSongMediaId(mediaId)) {
            for (Song s : AvailableSongs) {
                if (s.getPath().equals(getPathFromMediaId(mediaId)))
                    return s;
            }
        } else if (isLoopMediaId(mediaId)) {
            for(Song s : AvailableLoops) {
                if(s.getPath().equals(getPathFromMediaId(mediaId)))
                    return s;
            }
        } else if (isPlaylistMediaId(mediaId)) {
            for(Playlist p : AvailablePlaylists) {
                if(p.getPath().equals(getPathFromMediaId(mediaId)))
                    return p;
            }
        }
        Log.e("Mucify: ", "Invalid media id " + mediaId);
        return null;
    }

    public static Playback getPlaybackFromPath(File playbackPath) {
        for(Song s : AvailableSongs)
            if(s.getPath().equals(playbackPath))
                return s;

        for(Song s : AvailableLoops)
            if(s.getPath().equals(playbackPath))
                return s;

        for(Playlist s : AvailablePlaylists)
            if(s.getPath().equals(playbackPath))
                return s;
        return null;
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
            if(AvailablePlaylists.get(i).equals(playlist))
                return i;
        }
        return -1;
    }

    public static Song getSong(File songLoopPath) {
        for(Song s : AvailableSongs)
            if(s.getSongPath().equals(songLoopPath))
                return s;

        for(Song s : AvailableLoops)
            if(s.getLoopPath().equals(songLoopPath))
                return s;

        return null;
    }

    public static Playlist getPlaylist(File path) {
        for(Playlist p : AvailablePlaylists)
            if(p.getPath().equals(path))
                return p;
        return null;
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
                            if(extension.equals(LoopFileExtension)) {
                                try {
                                    AvailableLoops.add(new Song(file));
                                } catch (Song.LoadingFailedException e) {
//                                    Utils.startErrorActivity("Failed to load loop: " + file + "\n" + Utils.getDetailedError(e));
                                }
                            }
                        }
                        else if(playlist) {
                            String extension = FileManager.getFileExtension(file.getName());
                            if(extension.equals(PlaylistFileExtension)) {
                                AvailablePlaylists.add(new Playlist(file));
                            }
                        }
                    }
                }
            }
        }
    }
}