package com.de.mucify;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.de.mucify.player.Playback;
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

    public static final String LoopFileExtension = ".loop";
    public static final String PlaylistFileExtension = ".playlist";

    // MY_TODO: Does MediaPlayer support more audio formats? Does it support all of the listed ones? Do all of them work?
    public static final List<String> SupportedAudioExtensions = Arrays.asList(".3gp", ".mp4", ".m4a", ".aac", ".ts", ".flac", ".imy", ".mp3", ".mkv", ".ogg", ".wav");

    public static final ArrayList<Song> AvailableSongs = new ArrayList<>();
    public static final ArrayList<Song> AvailableLoops = new ArrayList<>();
    public static final ArrayList<Playlist> AvailablePlaylists = new ArrayList<>();

    private static final Comparator<Song> mSongComparator = (o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle());
    private static final Comparator<Playlist> mPlaylistComparator = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());

    public static void load(Context context) {
        DataDirectory = context.getFilesDir();
        MusicDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

        if (!DataDirectory.exists())
            DataDirectory.mkdirs();
    }

    public static void loadLoopsAndPlaylists(Context context, ThreadFinishedCallback onFinished) {
        new Thread(() -> {
            AvailablePlaylists.clear();
            AvailableLoops.clear();

            loadLoopsAndPlaylists(context);
            Collections.sort(AvailableLoops, mSongComparator);
            Collections.sort(AvailablePlaylists, mPlaylistComparator);
            onFinished.onFinished();
        }).start();
    }

    public static void loadSongs(Context context, ThreadFinishedCallback onFinished) {
        new Thread(() -> {
            AvailableSongs.clear();
            loadSongs(MusicDirectory, context);
            // MY_TODO: ConcurrentModificationException thrown here
            Collections.sort(AvailableSongs, mSongComparator);
            onFinished.onFinished();
        }).start();
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
            for (Song s : AvailableLoops) {
                if (s.getPath().equals(getPathFromMediaId(mediaId)))
                    return s;
            }
        } else if (isPlaylistMediaId(mediaId)) {
            for (Playlist p : AvailablePlaylists) {
                if (p.getPath().equals(getPathFromMediaId(mediaId)))
                    return p;
            }
        }
        Log.e("Mucify: ", "Invalid media id " + mediaId);
        return null;
    }

    public static Playback getPlaybackFromPath(File playbackPath) {
        for (Song s : AvailableSongs)
            if (s.getPath().equals(playbackPath))
                return s;

        for (Song s : AvailableLoops)
            if (s.getPath().equals(playbackPath))
                return s;

        for (Playlist s : AvailablePlaylists)
            if (s.getPath().equals(playbackPath))
                return s;
        return null;
    }


    public static int getSongIndex(Song song) {
        for (int i = 0; i < AvailableSongs.size(); ++i) {
            if (AvailableSongs.get(i).equalsUninitialized(song))
                return i;
        }
        return -1;
    }

    public static int getLoopIndex(Song song) {
        for (int i = 0; i < AvailableLoops.size(); ++i) {
            if (AvailableLoops.get(i).equalsUninitialized(song))
                return i;
        }
        return -1;
    }

    public static int getPlaylistIndex(Playlist playlist) {
        for (int i = 0; i < AvailablePlaylists.size(); ++i) {
            if (AvailablePlaylists.get(i).equals(playlist))
                return i;
        }
        return -1;
    }

    public static Song getSong(File songLoopPath) {
        for (Song s : AvailableSongs)
            if (s.getSongPath().equals(songLoopPath))
                return s;

        for (Song s : AvailableLoops)
            if (s.getLoopPath().equals(songLoopPath))
                return s;

        return null;
    }

    public static Playlist getPlaylist(File path) {
        for (Playlist p : AvailablePlaylists)
            if (p.getPath().equals(path))
                return p;
        return null;
    }


    private static void loadSongs(File path, Context context) {
        if (path == null || !path.exists())
            return;

        File[] files = path.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory())
                loadSongs(file, context);
            else {
                if (FileManager.isSongFile(file)) {
                    try {
                        AvailableSongs.add(new Song(context, file));
                    } catch (Song.LoadingFailedException e) {
                        e.printStackTrace();
                        // MY_TODO: Error handling
                    }
                }
            }
        }
    }

    private static void loadLoopsAndPlaylists(Context context) {
        if (DataDirectory == null || !DataDirectory.exists())
            return;

        File[] files = DataDirectory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (FileManager.isLoopFile(file)) {
                try {
                    AvailableLoops.add(new Song(context, file));
                } catch (Song.LoadingFailedException e) {
                    e.printStackTrace();
                    // MY_TODO: Error handling
                }
            } else if (FileManager.isPlaylistFile(file)) {
                AvailablePlaylists.add(new Playlist(context, file));
            }

        }
    }


    public interface ThreadFinishedCallback {
        void onFinished();
    }
}