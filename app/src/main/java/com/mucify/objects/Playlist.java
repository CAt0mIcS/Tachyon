package com.mucify.objects;

import com.mucify.Globals;

import java.io.File;
import java.util.ArrayList;

public class Playlist {

    private final ArrayList<Song> mSongs = new ArrayList<>();

    public static String toName(File file) {
        return file.getName().replace(Globals.PlaylistFileIdentifier, "").replace(Globals.PlaylistFileExtension, "");
    }
}
