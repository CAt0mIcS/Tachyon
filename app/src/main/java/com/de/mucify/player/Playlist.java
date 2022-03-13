package com.de.mucify.player;

import android.content.Context;

import com.de.mucify.FileManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Playlist extends Playback {
    private String mName;
    private Context mContext;
    private File mPlaylistFilePath;
    private ArrayList<Song> mSongs = new ArrayList<>();

    public Playlist(File path) {
        if(!path.exists()) {
//            Utils.startErrorActivity("Failed to load playlist: \"" + path + "\" does not exist.");
            return;
        }

        mPlaylistFilePath = path;
        mName = FileManager.playlistNameFromFile(path);
        loadPlaylist();
        for(Song s : mSongs)
            s.setLooping(false);
    }

    public Playlist(Context context, File path) {
        this(path);
        mContext = context;
        for(Song s : mSongs)
            s.setLooping(false);
    }

    public Playlist(File path, ArrayList<Song> songs) {
        mPlaylistFilePath = path;
        mSongs = songs;
        for(Song s : mSongs)
            s.setLooping(false);
    }

    public Playlist create(Context context) { mContext = context; return this; }

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

    public void delete() {
        mPlaylistFilePath.delete();
    }

    public String getName() { return mName; }
    public File getPlaylistFilePath() { return mPlaylistFilePath; }
    public ArrayList<Song> getSongs() { return mSongs; }
    public Context getContext() { return mContext; }

    private void loadPlaylist() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mPlaylistFilePath));

            while(reader.ready()) {
                try {
                    mSongs.add(new Song(new File(reader.readLine())));
                } catch(Song.LoadingFailedException e) {
                    // MY_TODO: Remove from playlist and display error message to user
                    e.printStackTrace();
                }
            }
            reader.close();

        } catch (IOException e) {
//            Utils.startErrorActivity("Failed to load playlist: " + mPlaylistFilePath + "\n" + Utils.getDetailedError(e));
        }
    }
}
