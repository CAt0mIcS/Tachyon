package com.mucify.objects;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mucify.Globals;
import com.mucify.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Song {
    private File mSongFilePath;
    protected File mLoopFilePath = null;
    private int mStartTime;
    private int mEndTime;

    protected MediaPlayer mMediaPlayer;

    public interface MediaPlayerFinishedListener {
        void onFinished(Song song);
    }

    private MediaPlayerFinishedListener mMediaPlayerFinishedListener;

    // path can be either a loop file or a song file
    public Song(Context context, File path) throws IOException {
        create(context, path);
    }

    public Song(Context context, File songFilePath, int startTime, int endTime) throws IOException {
        mStartTime = startTime;
        mEndTime = endTime;

        create(context, songFilePath);
    }

    public boolean isSame(Song song) {
        return mSongFilePath == song.mSongFilePath && mStartTime == song.mStartTime && mEndTime == song.mEndTime;
    }

    protected void create(Context context, File path) throws IOException {
        if(!path.exists()) {
            Utils.messageBox(context, "Error","File '" + path.getPath() + "' doesn't exist");
            return;
        }

        if(Utils.isLoopFile(path)) {
            parseLoopFile(path);
            mLoopFilePath = path;
        }
        else if(Utils.isSongFile(path)) {
            mSongFilePath = path;
        }
        else
            Utils.messageBox(context, "Error", "Invalid file type '" + path.getPath() + "'");

        if(!mSongFilePath.exists()) {
            Utils.messageBox(context, "Error", "File '" + mSongFilePath.getPath() + "' doesn't exist");
        }
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));

        if(mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();
    }

    public void update() {
        if(!mMediaPlayer.isPlaying())
            return;

        if(mMediaPlayer.getCurrentPosition() >= mEndTime || mMediaPlayer.getCurrentPosition() < mStartTime) {
            if(mMediaPlayerFinishedListener != null)
                mMediaPlayerFinishedListener.onFinished(this);
        }
    }

    public void start() {
        mMediaPlayer.start();
        seekTo(mStartTime);
    }

    public void seekTo(int millis) {
        mMediaPlayer.seekTo(millis, MediaPlayer.SEEK_CLOSEST);
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public String getName() {
        return Song.toName(mSongFilePath);
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public File getPath() {
        return mSongFilePath;
    }

    public File getLoopPath() {
        return mLoopFilePath;
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getStartTime() {
        return mStartTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    @NonNull
    @Override
    public String toString() {
        return toName(mSongFilePath);
    }

    public void setOnMediaPlayerFinishedListener(@NonNull MediaPlayerFinishedListener listener) {
        mMediaPlayerFinishedListener = listener;
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> listener.onFinished(Song.this));
    }

    public static String toName(File file) {
        return file.getName().replace(Utils.getFileExtension(file.getName()), "");
    }


    private void parseLoopFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        mSongFilePath = new File(reader.readLine());
        mStartTime = Integer.parseInt(reader.readLine());
        mEndTime = Integer.parseInt(reader.readLine());
        reader.close();
    }

    public void setStartTime(int millis) {
        mStartTime = millis;
        mMediaPlayer.seekTo(millis);
    }

    public void setEndTime(int millis) {
        mEndTime = millis;
    }
}
