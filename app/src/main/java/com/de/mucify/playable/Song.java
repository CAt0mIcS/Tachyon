package com.de.mucify.playable;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.util.FileManager;
import com.de.mucify.util.MediaLibrary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Song {

    private String mTitle;
    private String mArtist;

    private MediaPlayer mMediaPlayer;

    private int mStartTime;
    private int mEndTime;

    private File mSongFilePath;
    private File mLoopFilePath;

    /**
     * @param  path path to either a song audio file or a loop file
     */
    public Song(Context context, File path) {
        createInternal(context, path);
    }

    public Song(File path) {
        createInternal(path);
    }

    public Song(Context context, File songFilePath, int startTime, int endTime) {
        mStartTime = startTime;
        mEndTime = endTime;

        createInternal(context, songFilePath);
    }

    public void reset() {
        mMediaPlayer.reset();
    }

    public void start() {
        mMediaPlayer.start();
        // If MediaPlayer.SEEK_CLOSEST can't seek close enough, the song will "finish" at the beginning.
        // Threshold to prevent this
        seekTo(mStartTime + 20);
    }

    public void seekTo(int milliseconds) {
        mMediaPlayer.seekTo(milliseconds, MediaPlayer.SEEK_CLOSEST);
    }

    /**
     * Needs to be called only after @Song(File path) constructor was used
     */
    public void create(Context context) {
        createInternal(context, mSongFilePath);
    }

    public boolean isPlaying() { return mMediaPlayer.isPlaying(); }
    public void pause() { mMediaPlayer.pause(); }
    public void unpause() { mMediaPlayer.start(); }
    public String getTitle() { return mTitle; }
    public String getArtist() { return mArtist; }
    public File getSongPath() { return mSongFilePath; }
    public int getCurrentPosition() { return mMediaPlayer.getCurrentPosition(); }
    public int getDuration() { return mMediaPlayer.getDuration(); }
    public void setStartTime(int startTime) { mStartTime = startTime; }
    public void setEndTime(int endTime) { mEndTime = endTime; }
    public int getStartTime() { return mStartTime; }
    public int getEndTime() { return mEndTime; }
    public File getLoopPath() { return mLoopFilePath; }
    public String getLoopName() { return mLoopFilePath.getName().replace(MediaLibrary.LoopFileIdentifier, "").replace(MediaLibrary.LoopFileExtension, "").replace("_", " | "); }
    public void setVolume(float left, float right) { mMediaPlayer.setVolume(left, right); }
    public boolean isLoop() { return mLoopFilePath != null; }


    private void createInternal(Context context, File path) {
        createInternal(path);
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));
        mMediaPlayer.setLooping(true);

        if(mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();
    }

    private void createInternal(File path) {
        if(!path.exists()) {
            // MY_TODO: Add error message for user
            return;
        }

        if(FileManager.isLoopFile(path)) {
            try {
                parseLoopFile(path);
            } catch (IOException e) {
                // MY_TODO: Add error message for invalid file (format)
                return;
            }
            mLoopFilePath = path;
        }
        else if(FileManager.isSongFile(path)) {
            mSongFilePath = path;
        }
        else
            return;  // MY_TODO: Add invalid file type error message for user

        if(!mSongFilePath.exists()) {
            return;
            // MY_TODO: Add error message for user
        }

        mTitle = mSongFilePath.getName().replace(FileManager.getFileExtension(mSongFilePath.getName()), "");

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        try {
            metaRetriever.setDataSource(mSongFilePath.getAbsolutePath());
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        String artist =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if(artist != null)
            mArtist = artist;
        else
            mArtist = "Unknown Artist";
        if(title != null)
            mTitle = title;
    }

    private void parseLoopFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        mSongFilePath = new File(reader.readLine());
        mStartTime = Integer.parseInt(reader.readLine());
        mEndTime = Integer.parseInt(reader.readLine());
        reader.close();
    }
}
