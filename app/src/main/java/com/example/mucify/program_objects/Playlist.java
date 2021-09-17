package com.example.mucify.program_objects;

import android.content.Context;

import com.example.mucify.MainActivity;
import com.example.mucify.Util;
import com.example.mucify.ui.main.PlaylistFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Playlist {
    public ArrayList<Song> Songs = new ArrayList<>();
    public String Name;
    private File mFilepath;

    private MainActivity mActivity;

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
        Songs = songs;
        Name = name;
        mFilepath = new File(PlaylistFragment.PLAYLIST_IDENTIFIER + name + PlaylistFragment.PLAYLIST_EXTENSION);
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

                Songs.add(new Song(mActivity, file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().indexOf(PlaylistFragment.PLAYLIST_EXTENSION)), path, loopStartTime, loopEndTime));
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
            Util.messageBox(mActivity, "Error", e.getMessage());
        }
    }
}
