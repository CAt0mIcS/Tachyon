package com.de.mucify.player;

import android.content.Context;

import androidx.annotation.Nullable;

import com.de.mucify.FileManager;
import com.de.mucify.service.MediaPlaybackService;

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

    @Override
    public void start() {

    }

    @Override
    public void restart() {

    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void seekTo(int millis) {

    }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return 0;
    }

    @Override
    public void stop() {

    }

    @Override
    public void reset() {

    }

    @Override
    public String getTitle() {
        return ""; // return title of current song
    }

    @Override
    public String getSubtitle() {
        return ""; // return artist of current song
    }

    @Override
    public String getMediaId() {
        return getMediaId(this);
    }

    @Override
    public void create(Context context) {
        mContext = context;
    }

    @Override
    public Song next(Context context) {
        return null;
    }

    @Override
    public Song previous(Context context) {
        return null;
    }

    @Override
    public Song getCurrentSong() {
        return null;
    }

    @Override
    public void setVolume(float left, float right) {

    }

    public String getName() {
        return mName;
    }

    public static String getMediaId(Playlist playlist) {
        return "Playlist_" + MediaPlaybackService.Media.getPlaylistIndex(playlist);
    }

    public File getCurrentAudioPath() {
        return null;
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

    public void delete() {
        mPlaylistFilePath.delete();
    }

    public File getPlaylistFilePath() { return mPlaylistFilePath; }
    public ArrayList<Song> getSongs() { return mSongs; }
    public Context getContext() { return mContext; }

    public boolean equalsUninitialized(@Nullable Playlist playlist) {
        if(playlist == null)
            return false;

        return playlist.mName.equals(mName) &&
                playlist.mPlaylistFilePath.equals(mPlaylistFilePath) &&
                playlist.mSongs.equals(mSongs);
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
//            Utils.startErrorActivity("Failed to load playlist: " + mPlaylistFilePath + "\n" + Utils.getDetailedError(e));
        }
    }
}
