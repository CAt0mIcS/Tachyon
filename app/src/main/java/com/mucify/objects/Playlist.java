package com.mucify.objects;

import android.content.Context;

import com.mucify.Globals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Playlist {

    private final ArrayList<Song> mSongs = new ArrayList<>();
    private File mPath;
    private String mName;

    public Playlist(String name, ArrayList<Song> songs) {
        mName = name;
        mPath = Playlist.toFile(mName);
        mSongs.addAll(songs);
    }

    public Playlist(Context context, String name, File path) throws IOException {
        mPath = path;
        mName = name;
        parseFile(context);
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mPath));
        for(Song song : mSongs) {
            writer.write(song.getPath().getPath() + '\n');
            writer.write(String.valueOf(song.getStartTime()) + '\n');
            writer.write(String.valueOf(song.getEndTime()) + '\n');
        }
        writer.close();
    }

    public static String toName(File file) {
        return file.getName().replace(Globals.PlaylistFileIdentifier, "").replace(Globals.PlaylistFileExtension, "");
    }

    public static File toFile(String name) {
        return new File(Globals.DataDirectory + "/" + Globals.PlaylistFileIdentifier + name + Globals.PlaylistFileExtension);
    }

    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    private void parseFile(Context context) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(mPath));
        while(reader.ready()) {
            File songFilePath = new File(reader.readLine());
            int startTime = Integer.parseInt(reader.readLine());
            int endTime = Integer.parseInt(reader.readLine());

            mSongs.add(new Song(context, songFilePath, startTime, endTime));
        }
        reader.close();
    }
}
