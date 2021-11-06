package com.de.mucify.utils;

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
}
