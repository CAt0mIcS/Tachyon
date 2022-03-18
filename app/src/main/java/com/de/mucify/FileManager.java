package com.de.mucify;


import com.de.mucify.service.MediaPlaybackService;
import java.io.File;

public class FileManager {
    public static String getFileExtension(String filename) {
        if(!filename.contains("."))
            return "";
        return filename.substring(filename.lastIndexOf('.'));
    }

    public static boolean isLoopFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(MediaLibrary.LoopFileExtension);
    }

    public static boolean isSongFile(File path) {
        return path.isFile() && MediaLibrary.SupportedAudioExtensions.contains(getFileExtension(path.getName()));
    }

    public static boolean isPlaylistFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(MediaLibrary.PlaylistFileExtension);
    }

    public static String playlistNameFromFile(File path) {
        return path.getName().replace(MediaLibrary.PlaylistFileExtension, "");
    }

    public static File playlistNameToFile(String playlistName) {
        return new File(MediaPlaybackService.Media.DataDirectory + "/" + playlistName + MediaLibrary.PlaylistFileExtension);
    }

    public static File loopNameToFile(String loopName, String songName, String songAuthor) {
        return new File(MediaPlaybackService.Media.DataDirectory + "/" + loopName + "_" + songName + "_" + songAuthor + MediaLibrary.LoopFileExtension);
    }

    public static String loopNameFromFile(File path) {
        String name = path.getName().replace(MediaLibrary.LoopFileExtension, "");
        return name.substring(0, name.indexOf("_"));
    }
}