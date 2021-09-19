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
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Playlist {
    public final ArrayList<Song> Songs = new ArrayList<>();
    public String Name;
    private File mFilepath;
    private MainActivity mActivity;
    private boolean mPaused = false;
    private int mSongID = 0;

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
        if(!Songs.get(mSongID).isPlaying() && !mPaused) {

            Songs.get(mSongID).seekToOnce(0);

            if(randomizeOrder)
                mSongID = ThreadLocalRandom.current().nextInt(0, Songs.size());
            else {
                ++mSongID;
                if(mSongID >= Songs.size())
                    mSongID = 0;
            }


            Songs.get(mSongID).play(false);
        }
        // Song finished playing
        else if(Songs.get(mSongID).updateOnce()) {
            Songs.get(mSongID).pause();
        }
    }

    public void pause() {
        if(Songs.get(mSongID).isPlaying()) {
            Songs.get(mSongID).pause();
            mPaused = true;
        }
    }

    public boolean isPaused() {
        return mPaused;
    }

    public void resume() {
        mPaused = false;
        Songs.get(mSongID).play(false);
    }

    public void playSong(int position) {
        mPaused = false;
        Songs.get(mSongID).seekToOnce(0);
        Songs.get(mSongID).pause();

        mSongID = position;
        Songs.get(mSongID).seekToOnce(Songs.get(mSongID).getStartTime());
        Songs.get(mSongID).play(false);
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

    public int getCurrentPosition() {
        if(Songs.get(mSongID).isPlaying())
            return Songs.get(mSongID).getCurrentPosition();
        return 0;
    }

    public int getDuration() {
        if(Songs.get(mSongID).isPlaying())
            return Songs.get(mSongID).getDuration();
        return 0;
    }

    public void seekTo(int progress) {
        if(Songs.get(mSongID).isPlaying())
            Songs.get(mSongID).seekToOnce(progress);
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
