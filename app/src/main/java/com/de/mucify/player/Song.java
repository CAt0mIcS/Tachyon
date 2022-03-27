package com.de.mucify.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.de.mucify.FileManager;
import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.service.MediaPlaybackService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class Song extends Playback {
    private String mTitle;
    private String mArtist = null;
    private Bitmap mSongImage;

    private MediaPlayer mMediaPlayer;

    private int mStartTime;
    private int mEndTime;

    private File mSongFilePath;
    private File mLoopFilePath;

    /**
     * Creates a Song object which only has basic data (title, artist, path, startTime, endTime) set.
     * Call create() to make this Song playable.
     *
     * @param path to a song or loop file.
     * @throws LoadingFailedException if the path doesn't exist or if the loading of the loop file fails.
     */
    public Song(Context context, File path) throws LoadingFailedException {
        if (path == null || !path.exists()) {
            throw new LoadingFailedException("Failed to load song: \"" + path + "\" does not exist");
        }

        if (FileManager.isLoopFile(path)) {
            try {
                parseLoopFile(path);
            } catch (IOException e) {
                throw new LoadingFailedException("Failed to load loop: \"" + mSongFilePath + "\" is not a valid loop file");
            }
            mLoopFilePath = path;
        } else if (FileManager.isSongFile(path)) {
            mSongFilePath = path;
        } else {
            throw new LoadingFailedException("Failed to load song: \"" + mSongFilePath + "\" is an invalid file type");
        }

        if (!mSongFilePath.exists()) {
            throw new LoadingFailedException("Failed to load song: \"" + mSongFilePath + "\" does not exist");
        }

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(mSongFilePath.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        String artist = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

        byte[] art = metaRetriever.getEmbeddedPicture();
        if (art != null) {
            mSongImage = BitmapFactory.decodeByteArray(art, 0, art.length);
        }


        if (title != null)
            mTitle = title;
        else
            mTitle = mSongFilePath.getName().replace(FileManager.getFileExtension(mSongFilePath.getName()), "");

        if (artist != null)
            mArtist = artist;
        else
            mArtist = context.getString(R.string.unknown_artist);
    }

    /**
     * Creates a fully created Song object which can be played directly after the constructor
     *
     * @param songFilePath path to a Song file
     * @param startTime    offset in milliseconds when the song should start playing
     * @param endTime      offset in milliseconds when the song should stop playing
     * @throws LoadingFailedException if the path doesn't exist or if the loading of the loop file fails.
     */
    public Song(Context context, File songFilePath, int startTime, int endTime) throws LoadingFailedException {
        this(context, songFilePath);
        mStartTime = startTime;
        mEndTime = endTime;
    }

    public boolean isArtistUnknown() {
        return mArtist == null;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(int millis) {
        mMediaPlayer.seekTo(millis);
        Log.d("Mucify.Song", "Song.seekTo " + millis);
    }

    @Override
    public void start(Context context) {
        mMediaPlayer.start();
        Log.d("Mucify.Song", "Song.start");
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
        Log.d("Mucify.Song", "Song.pause");
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public Bitmap getImage() {
        return mSongImage;
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
        Log.d("Mucify.Song", "Song.stop");
    }

    @Override
    public void reset() {
        mMediaPlayer.release();
        mMediaPlayer = null;
        Log.d("Mucify.Song", "Song.reset");
    }

    @Override
    public void create(Context context) {
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));

        if (mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();

        Log.d("Mucify.Song", "Song.create");
    }

    @Override
    public String getMediaId() {
        if (isLoop())
            return "Loop_" + getLoopPath();
        return "Song_" + getSongPath();
    }

    @Override
    public Playback next(Context context) {
        Log.d("Mucify.Song", "Song.next");

        Song s;
        if (!isLoop()) {
            int idx = MediaLibrary.getSongIndex(this) + 1;
            if (idx >= MediaLibrary.AvailableSongs.size())
                idx = 0;
            s = MediaLibrary.AvailableSongs.get(idx);
        } else {
            int idx = MediaLibrary.getLoopIndex(this) + 1;
            if (idx >= MediaLibrary.AvailableLoops.size())
                idx = 0;
            s = MediaLibrary.AvailableLoops.get(idx);
        }
        s.create(context);
        return s;
    }

    @Override
    public Playback previous(Context context) {
        Log.d("Mucify.Song", "Song.previous");

        Song s;
        if (!isLoop()) {
            int idx = MediaLibrary.getSongIndex(this) - 1;
            if (idx < 0)
                idx = MediaLibrary.AvailableSongs.size() - 1;
            s = MediaLibrary.AvailableSongs.get(idx);
        } else {
            int idx = MediaLibrary.getLoopIndex(this) - 1;
            if (idx < 0)
                idx = MediaLibrary.AvailableLoops.size() - 1;
            s = MediaLibrary.AvailableLoops.get(idx);
        }
        s.create(context);
        return s;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getDurationUninitialized() {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(mSongFilePath.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        String durationStr = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return Integer.parseInt(durationStr);
    }

    public int getEndTimeUninitialized() {
        if (mEndTime == 0)
            return getDurationUninitialized();
        return mEndTime;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSubtitle() {
        return getArtist();
    }

    @Override
    public boolean isCreated() {
        return mMediaPlayer != null;
    }

    @Override
    public void setVolume(float left, float right) {
        mMediaPlayer.setVolume(left, right);
    }

    @Override
    public Song getCurrentSong() {
        return this;
    }

    /**
     * @return path to either song or loop file
     */
    @Override
    public File getPath() {
        if (isLoop())
            return getLoopPath();
        return getSongPath();
    }

    public void setStartTime(int startTime) {
        mStartTime = startTime;
    }

    public void setEndTime(int endTime) {
        mEndTime = endTime;
    }

    public String getArtist() {
        return mArtist;
    }

    public File getSongPath() {
        return mSongFilePath;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public File getLoopPath() {
        return mLoopFilePath;
    }

    public String getLoopName() {
        return FileManager.loopNameFromFile(mLoopFilePath);
    }

    public boolean isLoop() {
        return mLoopFilePath != null;
    }

    public void setOnMediaPlayerCompletionListener(MediaPlayer.OnCompletionListener listener) {
        mMediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * Checks if two Song objects are equal in all their uninitialized data. Doesn't require
     * Song to be created.
     */
    public boolean equalsUninitialized(@Nullable Song obj) {
        if (obj == null)
            return false;
        boolean equals = mTitle.equals(obj.mTitle) && mArtist.equals(obj.mArtist) && mStartTime == obj.mStartTime && mSongFilePath.equals(obj.mSongFilePath);
        if (equals && mLoopFilePath != null && obj.mLoopFilePath != null)
            equals = mLoopFilePath.equals(obj.mLoopFilePath);
        else if (mLoopFilePath == null && obj.mLoopFilePath == null) {
        } else equals = false;
        return equals;
    }

    /**
     * Checks if two Song objects are equal. Requires Song to be created.
     */
    public boolean equals(@Nullable Song obj) {
        if (obj == null)
            return false;
        boolean equals = mTitle.equals(obj.mTitle) && mArtist.equals(obj.mArtist) && mStartTime == obj.mStartTime && mEndTime == obj.mEndTime && mMediaPlayer == obj.mMediaPlayer && mSongFilePath.equals(obj.mSongFilePath);
        if (equals && mLoopFilePath != null && obj.mLoopFilePath != null)
            equals = mLoopFilePath.equals(obj.mLoopFilePath);
        else if (mLoopFilePath == null && obj.mLoopFilePath == null) {
        } else equals = false;
        return equals;
    }

    /**
     * Saves the song with start time and end time to a file.
     */
    public void saveAsLoop(String loopName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FileManager.loopNameToFile(loopName, getTitle(), getArtist())));
            writer.write(getSongPath().getPath() + '\n');
            writer.write(String.valueOf(getStartTime()) + '\n');
            writer.write(String.valueOf(getEndTime()) + '\n');
            writer.close();
            Log.d("Mucify.Song", "Saved loop with start: " + getStartTime() + " end: " + getEndTime() + " with name " + loopName);
        } catch (IOException e) {
            e.printStackTrace();
            // MY_TODO: Error handling
        }
    }

    /**
     * Parses the loop file. Doesn't do any checking if the file is correctly formatted.
     *
     * @param file path to loop file
     * @throws IOException if reading fails
     */
    private void parseLoopFile(File file) throws IOException {
        Log.d("Mucify.Song", "Parsing loop file " + file);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        mSongFilePath = new File(reader.readLine());
        mStartTime = Integer.parseInt(reader.readLine());
        mEndTime = Integer.parseInt(reader.readLine());
        reader.close();
    }

    public static class LoadingFailedException extends Exception {
        public LoadingFailedException(String msg) {
            super(msg);
        }
    }
}
