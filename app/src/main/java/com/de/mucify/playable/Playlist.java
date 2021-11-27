package com.de.mucify.playable;

import android.content.Context;

import com.de.mucify.util.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Playlist {
    private String mName;
    private File mPlaylistFilePath;
    private ArrayList<Song> mSongs = new ArrayList<>();

    public Playlist(File path) {
        if(!path.exists()) {
            // MY_TODO: Error message
            return;
        }

        mPlaylistFilePath = path;
        mName = FileManager.playlistNameFromFile(path);
        loadPlaylist();
    }

    public String getName() { return mName; }
    public File getPlaylistFilePath() { return mPlaylistFilePath; }
    public ArrayList<Song> getSongs() { return mSongs; }


    private void loadPlaylist() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mPlaylistFilePath));

            while(reader.ready()) {
                mSongs.add(new Song(new File(reader.readLine())));
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            // MY_TODO: Error message, invalid file
        }
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mPlaylistFilePath));

        for(Song song : mSongs) {
            if(song.isLoop())
                writer.write(song.getLoopPath() + "\n");
            else
                writer.write(song.getSongPath() + "\n");
        }
        writer.close();
    }
}
