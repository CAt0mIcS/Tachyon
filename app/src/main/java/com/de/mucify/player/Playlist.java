package com.de.mucify.player;

import android.content.Context;
import android.media.MediaPlayer;

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
import java.util.Objects;

public class Playlist extends Playback {
    private String mName;
    private File mPlaylistFilePath;
    private final ArrayList<Song> mSongs = new ArrayList<>();
    private int mCurrentSongIndex = 0;


    /**
     * Creates a Playlist. Afterwards you'll need to call create() and start() to play the first song.
     * You also need to set the context using setContext().
     *
     * @param path path to the playlist file
     */
    public Playlist(Context context, File path) {
        if (!path.exists()) {
//            Utils.startErrorActivity("Failed to load playlist: \"" + path + "\" does not exist.");
            return;
        }

        mPlaylistFilePath = path;
        mName = FileManager.playlistNameFromFile(path);
        loadPlaylist(context);
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
        return "Playlist_" + getPath();
    }

    @Override
    public void create(Context context) {
        getCurrentSong().create(context);
    }

    @Override
    public Playback next(Context context) {
        mCurrentSongIndex++;
        if (mCurrentSongIndex >= mSongs.size())
            mCurrentSongIndex = 0;

        getCurrentSong().create(context);
        return this;
    }

    @Override
    public Playback previous(Context context) {
        mCurrentSongIndex--;
        if (mCurrentSongIndex < 0)
            mCurrentSongIndex = mSongs.size() - 1;

        getCurrentSong().create(context);
        return this;
    }

    @Override
    public Song getCurrentSong() {
        return mSongs.get(mCurrentSongIndex);
    }

    /**
     * @return the path to the playlist file
     */
    @Override
    public File getPath() {
        return mPlaylistFilePath;
    }

    @Override
    public void setVolume(float left, float right) {
        getCurrentSong().setVolume(left, right);
    }

    @Override
    public boolean isCreated() {
        return getCurrentSong().isCreated();
    }

    /**
     * @return the Playlist's name.
     */
    public String getName() {
        return mName;
    }

    /**
     * @return the path of the currently playing song. If the current song is a loop the loop path
     * will be returned. If the current song is a song the song path will be returned.
     */
    public File getCurrentAudioPath() {
        if (getCurrentSong().isLoop())
            return getCurrentSong().getLoopPath();
        return getCurrentSong().getSongPath();
    }

    /**
     * Saves the entire playlist to the path set in the constructor. Overwrites any previously
     * existing file at playlist path
     *
     * @throws IOException writing failed.
     */
    public void save() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(mPlaylistFilePath));

        for (Song song : mSongs) {
            if (song.isLoop())
                writer.write(song.getLoopPath() + "\n");
            else
                writer.write(song.getSongPath() + "\n");
        }
        writer.close();
    }

    public static void save(File path) throws IOException {
        path.createNewFile();
    }

    /**
     * Deletes the playlist file at the path specified in the constructor
     */
    public void delete() {
        mPlaylistFilePath.delete();
    }

    /**
     * @return list of all songs in the playlist
     */
    public ArrayList<Song> getSongs() {
        return mSongs;
    }

    /**
     * @return the total length in milliseconds of all songs combined
     */
    public int getLength() {
        int length = 0;
        for (Song s : mSongs)
            length += s.isCreated() ? s.getDuration() : s.getDurationUninitialized();
        return length;
    }

    /**
     * Adds a new song to the playlist if it doesn't exist yet
     */
    public void addSong(Song song) {
        for (Song s : mSongs)
            if (s.equalsUninitialized(song) || song == null)
                return;

        mSongs.add(song);
    }

    /**
     * Removes a existing song from the playlist.
     */
    public void removeSong(Song song) {
        for (Song s : mSongs) {
            if (s.equalsUninitialized(song)) {
                mSongs.remove(s);
                return;
            }
        }
    }

    /**
     * Sets the Song to be played to @param song. Does nothing if the Song doesn't exist.
     * Only sets the index in the song list to be played next. A call to create and start will actually
     * start the set song.
     */
    public void setCurrentSong(Song song) {
        for (int i = 0; i < mSongs.size(); ++i)
            if (song.equalsUninitialized(mSongs.get(i))) {
                mCurrentSongIndex = i;
                return;
            }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mName, mPlaylistFilePath, mSongs, mCurrentSongIndex);
    }

    /**
     * Checks if two Playlists are equal
     */
    public boolean equals(@Nullable Playlist playlist) {
        if (playlist == null)
            return false;

        return playlist.mName.equals(mName) &&
                playlist.mPlaylistFilePath.equals(mPlaylistFilePath) &&
                playlist.mSongs.equals(mSongs);
    }

    /**
     * Parses the playlist file set in the constructor. Doesn't do any checking if the file is correctly formatted.
     */
    private void loadPlaylist(Context context) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mPlaylistFilePath));

            while (reader.ready()) {
                try {
                    mSongs.add(new Song(context, new File(reader.readLine())));
                } catch (Song.LoadingFailedException e) {
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
