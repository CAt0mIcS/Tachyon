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
    private File mPlaylistFilePath;
    private ArrayList<Song> mSongs = new ArrayList<>();
    private int mCurrentSongIndex = 0;

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

    public Playlist(File path, ArrayList<Song> songs) {
        mPlaylistFilePath = path;
        mSongs = songs;
        for(Song s : mSongs)
            s.setLooping(false);
    }

    @Override
    public void start(Context context) {
        getCurrentSong().start(context);
    }

    @Override
    public void pause() {
        getCurrentSong().pause();
    }

    @Override
    public boolean isPlaying() {
        return getCurrentSong().isPlaying();
    }

    @Override
    public void seekTo(int millis) {
        getCurrentSong().seekTo(millis);
    }

    @Override
    public int getDuration() {
        return getCurrentSong().getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return getCurrentSong().getCurrentPosition();
    }

    @Override
    public void stop() {
            getCurrentSong().stop();
    }

    @Override
    public void reset() {
        getCurrentSong().reset();
    }

    @Override
    public String getTitle() {
        return getCurrentSong().getTitle();
    }

    @Override
    public String getSubtitle() {
        return getCurrentSong().getSubtitle();
    }

    @Override
    public String getMediaId() {
        return getMediaId(this);
    }

    @Override
    public void create(Context context) {
        getCurrentSong().create(context);
    }

    @Override
    public Playback next(Context context) {
        mCurrentSongIndex++;
        if(mCurrentSongIndex >= mSongs.size())
            mCurrentSongIndex = 0;

        getCurrentSong().create(context);
        return this;
    }

    @Override
    public Playback previous(Context context) {
        mCurrentSongIndex--;
        if(mCurrentSongIndex < 0)
            mCurrentSongIndex = mSongs.size() - 1;

        getCurrentSong().create(context);
        return this;
    }

    @Override
    public Song getCurrentSong() {
        return mSongs.get(mCurrentSongIndex);
    }

    @Override
    public void setVolume(float left, float right) {
        getCurrentSong().setVolume(left, right);
    }

    @Override
    public boolean isCreated() {
        return getCurrentSong().isCreated();
    }

    public String getName() {
        return mName;
    }

    public static String getMediaId(Playlist playlist) {
        return "Playlist_" + MediaPlaybackService.Media.getPlaylistIndex(playlist);
    }

    public File getCurrentAudioPath() {
        if(getCurrentSong().isLoop())
            return getCurrentSong().getLoopPath();
        return getCurrentSong().getSongPath();
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
