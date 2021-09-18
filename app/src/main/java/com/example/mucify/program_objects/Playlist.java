package com.example.mucify.program_objects;

import android.content.Context;

import com.example.mucify.MainActivity;
import com.example.mucify.Util;
import com.example.mucify.ui.main.PlaylistFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class Playlist {
    public final ArrayList<Song> Songs = new ArrayList<>();
    public String Name;
    private File mFilepath;
    private MainActivity mActivity;
    private boolean mPaused = false;

    public Playlist(MainActivity activity, String name, File file) {
        if(!file.exists())
            throw new IllegalArgumentException("Directory '" + file.getAbsolutePath() + "' does not exist.");

        mActivity = activity;
        Name = name;
        mFilepath = file;

        parseFile(file);
    }

    public Playlist(MainActivity activity, String name, ArrayList<Song> songs) {
        mActivity = activity;

        // Don't reference ArrayList
        Songs.addAll(songs);

        Name = name;
        mFilepath = new File(PlaylistFragment.PLAYLIST_IDENTIFIER + name + PlaylistFragment.PLAYLIST_EXTENSION);
    }

    public void update(boolean randomizeOrder) {
        if(!Songs.get(0).isPlaying() && !mPaused) {
            if(randomizeOrder)
                Collections.shuffle(Songs);

            Songs.get(0).play(false);
        }
        // Song finished playing
        if(Songs.get(0).updateOnce()) {
            Songs.get(0).pause();
        }
    }

    public void pause() {
        if(Songs.get(0).isPlaying()) {
            Songs.get(0).pause();
            mPaused = true;
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void resume() {
        mPaused = false;
        Songs.get(0).play(false);
    }

    public void save() throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(
                mActivity.getApplicationContext().openFileOutput(mFilepath.getPath(), Context.MODE_PRIVATE));

        for(Song song : Songs) {
            writer.write(song.Path + "\n");  // Path to music file
            writer.write(song.getStartTime() + "\n");  // Loop start time in seconds
            writer.write(song.getEndTime() + "\n");  // Loop end time in seconds
        }
        writer.close();

    }

    public void reset() {
        for (Song song : Songs) {
            song.reset();
        }
    }

    private void parseFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            while(reader.ready()) {
                String path = reader.readLine();
                int loopStartTime = Integer.parseInt(reader.readLine());
                int loopEndTime = Integer.parseInt(reader.readLine());

                Songs.add(new Song(mActivity, path.substring(path.lastIndexOf("/") + 1, path.indexOf(Util.getFileExtension(path).get())), path, loopStartTime, loopEndTime));
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            Util.messageBox(mActivity, "Error", e.getMessage());
        }
    }
}
