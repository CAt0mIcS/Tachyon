package com.de.mucify.playable;

import android.content.Context;

import com.de.mucify.util.FileManager;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Playlist {
    private String mName;
    private Context mContext;
    private File mPlaylistFilePath;
    private ArrayList<Song> mSongs = new ArrayList<>();
    private ArrayList<Song> mPlayingSongs = new ArrayList<>();

    private Song mPreviousSong;

    public Playlist(File path) {
        if(!path.exists()) {
            Utils.startErrorActivity("Failed to load playlist: \"" + path + "\" does not exist.");
            return;
        }

        mPlaylistFilePath = path;
        mName = FileManager.playlistNameFromFile(path);
        loadPlaylist();
        for(Song s : mSongs)
            s.setLooping(false);
        addSongs();
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

    public void reset() {
        for(Song s : mSongs)
            if(s.isCreated())
                s.reset();
        mSongs.clear();
    }

    public Song next() {
        if(mSongs.size() == 1)
            return mPlayingSongs.get(0);

        mPreviousSong = mPlayingSongs.remove(0);
        mPreviousSong.reset();
        if(mPlayingSongs.size() == 0)
            addSongs();
        Song newSong = mPlayingSongs.get(0);
        try {
            newSong.create(mContext);
        } catch (Song.LoadingFailedException e) {
            Utils.startErrorActivity("Failed to load song: " + newSong.getSongPath() + "\n" + Utils.getDetailedError(e));
        }
        return newSong;
    }

    public Song previous() {
        if(mPreviousSong == null || mSongs.size() == 1)
            return null;

        Song oldSong = mPlayingSongs.remove(0);
        oldSong.reset();

        for(Song song : mSongs) {
            if(song.equals(mPreviousSong)) {
                mPlayingSongs.add(0, song);
                try {
                    mPlayingSongs.get(0).create(mContext);
                } catch (Song.LoadingFailedException e) {
                    Utils.startErrorActivity("Failed to load song: " + mPlayingSongs.get(0).getSongPath() + "\n" + Utils.getDetailedError(e));
                }
                mPreviousSong = oldSong;
                return mPlayingSongs.get(0);
            }
        }
        return null;
    }

    public boolean setNextSong(Song song) {
        for(Song s : mPlayingSongs) {
            if(s.equalsUninitialized(song)) {
                mPlayingSongs.remove(s);
                mPlayingSongs.add(1, s);
                return true;
            }
        }

        for(Song s : mSongs) {
            if(s.equalsUninitialized(song)) {
                mPlayingSongs.add(1, s);
                return true;
            }
        }
        return false;
    }

    public String getName() { return mName; }
    public File getPlaylistFilePath() { return mPlaylistFilePath; }
    public ArrayList<Song> getSongs() { return mSongs; }
    public ArrayList<Song> getPlayingSongs() { return mPlayingSongs; }
    public Context getContext() { return mContext; }


    private void addSongs() {
        mPlayingSongs.clear();
        mPlayingSongs.addAll(mSongs);
        if(UserSettings.RandomizePlaylistSongOrder)
            Collections.shuffle(mPlayingSongs);
    }

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
            Utils.startErrorActivity("Failed to load playlist: " + mPlaylistFilePath + "\n" + Utils.getDetailedError(e));
        }
    }
}
