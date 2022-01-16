package com.de.mucify.util;

import android.provider.MediaStore;

import java.io.File;
import java.util.Optional;

public class FileManager {
    public static String getFileExtension(String filename) {
        Optional<String> opt = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));

        return opt.orElse("");
    }

    public static boolean isLoopFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(MediaLibrary.LoopFileExtension) && path.getName().indexOf(MediaLibrary.LoopFileIdentifier) == 0;
    }

    public static boolean isSongFile(File path) {
        return path.isFile() && MediaLibrary.SupportedAudioExtensions.contains(getFileExtension(path.getName()));
    }

    public static boolean isPlaylistFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(MediaLibrary.PlaylistFileExtension) && path.getName().indexOf(MediaLibrary.PlaylistFileIdentifier) == 0;
    }

    public static String playlistNameFromFile(File path) {
        return path.getName().replace(MediaLibrary.PlaylistFileExtension, "").replace(MediaLibrary.PlaylistFileIdentifier, "");
    }

    public static File playlistNameToFile(String playlistName) {
        return new File(MediaLibrary.DataDirectory + "/" + MediaLibrary.PlaylistFileIdentifier + playlistName + MediaLibrary.PlaylistFileExtension);
    }

    public static File loopNameToFile(String loopName, String songName, String songAuthor) {
        return new File(MediaLibrary.DataDirectory + "/" + MediaLibrary.LoopFileIdentifier + loopName + "_" + songName + "_" + songAuthor + MediaLibrary.LoopFileExtension);
    }

    public static String loopNameFromFile(File path) {
        String name = path.getName().replace(MediaLibrary.LoopFileIdentifier, "").replace(MediaLibrary.LoopFileExtension, "");
        return name.substring(0, name.indexOf("_"));
    }
}
