package com.de.mucify.playable;

import android.content.Context;

import com.de.mucify.util.FileManager;
import com.de.mucify.util.UserSettings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.DoubleStream;

public class Playlist {
    private String mName;
    private File mPlaylistFilePath;

    private ArrayList<Song> mSongs = new ArrayList<>();
    private ArrayList<Song> mPlayingSongs = new ArrayList<>();

    public Playlist(File path) {
        if(!path.exists()) {
            // MY_TODO: Error message
            return;
        }

        mPlaylistFilePath = path;
        mName = FileManager.playlistNameFromFile(path);
    }

    public Playlist(Context context, File path) {
        this(path);
        create(context);
    }

    /**
     * Needs to be called only after @Playlist(File path) constructor was used
     */
    public void create(Context context) {
        loadPlaylist(context);
    }

    public String getName() { return mName; }
    public File getPlaylistFilePath() { return mPlaylistFilePath; }
    public ArrayList<Song> getSongs() { return mSongs; }

    public Song start() {
        if(mPlayingSongs.size() == 0)
            addAllSongs();
        Song newSong = mPlayingSongs.get(0);
        mPlayingSongs.remove(0);
        return newSong;
    }

    public void reset() {
        for(Song song : mSongs)
            song.reset();
    }

    private void addAllSongs() {
        mPlayingSongs.addAll(mSongs);
        if(UserSettings.RandomizePlaylistSongOrder)
            Collections.shuffle(mPlayingSongs);
    }

    private void loadPlaylist(Context context) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mPlaylistFilePath));

            while(reader.ready()) {
                String path = reader.readLine();
                int loopStartTime = Integer.parseInt(reader.readLine());
                int loopEndTime = Integer.parseInt(reader.readLine());

                mSongs.add(new Song(context, new File(path), loopStartTime, loopEndTime));
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            // MY_TODO: Error message, invalid file
        }
    }

    public void addSong(Song song) {
        mSongs.add(song);
        mPlayingSongs.add(song);
    }

    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mPlaylistFilePath));

        for(Song song : mSongs) {
            writer.write(song.getSongPath() + "\n");  // Path to music file
            writer.write(song.getStartTime() + "\n");  // Loop start time
            writer.write(song.getEndTime() + "\n");  // Loop end time
        }
        writer.close();
    }

    public static void createNew(Context context, String playlistName) throws IOException {
        File path = FileManager.playlistNameToFile(playlistName);
        if(!path.createNewFile())
            throw new IOException("Failed to create new file.");
    }
}
