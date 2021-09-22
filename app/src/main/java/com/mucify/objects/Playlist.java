package com.mucify.objects;

import android.content.Context;

import com.mucify.Globals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Playlist {

    private final ArrayList<Song> mSongs = new ArrayList<>();
    private ArrayList<Song> mSongsToPlay = new ArrayList<>();
    private File mPath;
    private String mName;
    private NewSongListener mNewSongListener = null;

    public interface NewSongListener {
        void onStarted(Song song);
    }

    public Playlist(String name, ArrayList<Song> songs) {
        mName = name;
        mPath = Playlist.toFile(mName);
        mSongs.addAll(songs);

        setup();
    }

    public Playlist(Context context, String name) throws IOException {
        mPath = Playlist.toFile(name);
        mName = name;
        parseFile(context);

        setup();
    }

    public Playlist(Context context, File file) throws IOException {
        mPath = file;
        mName = Playlist.toName(file);
        parseFile(context);

        setup();
    }

    public void start() {
        mSongsToPlay.get(0).start();
        if(mNewSongListener != null)
            mNewSongListener.onStarted(mSongsToPlay.get(0));
    }

    public void update() {
        mSongsToPlay.get(0).update();
    }

    public void play(int position) {
        if(mSongsToPlay.get(0).isPlaying()) {
            mSongsToPlay.get(0).pause();
            mSongsToPlay.get(0).seekTo(0);
        }
        mSongsToPlay.remove(mSongs.get(position));
        mSongsToPlay.add(0, mSongs.get(position));
        start();
    }

    public boolean isSong(int position) {
        return mSongs.get(position).getStartTime() == 0;
    }

    public Song getCurrentSong() {
        return mSongsToPlay.get(0);
    }

    public void setOnNewSongListener(NewSongListener l) { mNewSongListener = l; }

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
        ArrayList<Song> songs = new ArrayList<>();
        for(Song s : mSongs) {
            if(s.getStartTime() == 0 && s.getEndTime() == s.getDuration())
                songs.add(s);
        }
        return songs;
    }

    public ArrayList<Song> getLoops() {
        ArrayList<Song> loops = new ArrayList<>();
        for(Song s : mSongs) {
            if(s.getStartTime() != 0 || s.getEndTime() != s.getDuration())
                loops.add(s);
        }
        return loops;
    }

    public ArrayList<Song> getSongsAndLoops() {
        return mSongs;
    }

    private void setup() {
        mSongsToPlay.addAll(mSongs);
        Collections.shuffle(mSongsToPlay);

        for(Song song : mSongs) {
            song.setOnMediaPlayerFinishedListener(mediaPlayer -> {
                mSongsToPlay.remove(0);

                if(mSongsToPlay.size() == 0) {
                    mSongsToPlay.addAll(mSongs);
                    Collections.shuffle(mSongsToPlay);
                }

                // Pause all still playing media players
                for(Song s : getSongsAndLoops()) {
                    if(s.isPlaying()) {
                        s.seekTo(s.getStartTime());
                        s.pause();
                    }
                }

                mSongsToPlay.get(0).start();
                if(mNewSongListener != null)
                    mNewSongListener.onStarted(mSongsToPlay.get(0));
            });
        }
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
