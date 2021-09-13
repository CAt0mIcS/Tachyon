package com.example.mucify.program_objects;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

public class Song {
    public String Name;
    public String Path;

    // Start and end times in milliseconds
    private int mStartTime = 0;
    private int mEndTime = 0;

    private MediaPlayer mMediaPlayer;

    public Song(Context context, String name, String path) {
        Name = name;
        Path = path;

        Uri uri = Uri.parse(path);
        mMediaPlayer = MediaPlayer.create(context, uri);
    }

    // Start and end times in milliseconds
    public void play(int startTime, int endTime) {
        mStartTime = startTime;
        mEndTime = endTime;

        mMediaPlayer.start();
        mMediaPlayer.seekTo(mStartTime, MediaPlayer.SEEK_CLOSEST);
        mMediaPlayer.setLooping(true);
    }

    public void update() {
        if(mMediaPlayer != null) {
            if(mMediaPlayer.getCurrentPosition() >= mEndTime || mMediaPlayer.getCurrentPosition() < mStartTime || !mMediaPlayer.isPlaying()) {
                mMediaPlayer.seekTo(mStartTime, MediaPlayer.SEEK_CLOSEST);
                if (!mMediaPlayer.isPlaying())
                    mMediaPlayer.start();
            }
        }
    }

    public void pause() {
        mMediaPlayer.pause();
    }

    public void seekTo(int millisec) {
        mMediaPlayer.seekTo(millisec);
        update();
    }

    public void release()
    {
        mMediaPlayer.release();
    }

    // In milliseconds
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getStartTime() { return mStartTime; }
    public void setStartTime(int t)  {
        if(mStartTime > mEndTime) {
            mStartTime = mEndTime;
            return;
        }

        mStartTime = t;
        play(mStartTime, mEndTime);
    }
    public int getEndTime() { return mEndTime; }
    public void setEndTime(int t)  { mEndTime = t; update(); }
}
