package com.example.mucify.program_objects;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

public class Song {
    public String Name;
    public String Path;

    // Start and end times in milliseconds
    private int mStartTime = 0;
    private int mEndTime = 0;

    private final Object mLock = new Object();

    private MediaPlayer mMediaPlayer;

    public Song(Context context, String name, String path) {
        Name = name;
        Path = path;

        Uri uri = Uri.parse(path);
        synchronized (mLock) {
            mMediaPlayer = MediaPlayer.create(context, uri);
        }
    }

    public Song(Context context, String name, String path, int startTime, int endTime) {
        Name = name;
        Path = path;
        mStartTime = startTime;
        mEndTime = endTime;

        Uri uri = Uri.parse(path);
        synchronized (mLock) {
            mMediaPlayer = MediaPlayer.create(context, uri);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return Name;
    }

    // Start and end times in milliseconds
    public void play(int startTime, int endTime) {
        mStartTime = startTime;
        mEndTime = endTime;

        synchronized (mLock) {
            if(mMediaPlayer != null) {
                mMediaPlayer.start();
                mMediaPlayer.seekTo(mStartTime, MediaPlayer.SEEK_CLOSEST);
                mMediaPlayer.setLooping(true);
            }
        }
    }

    synchronized public void play(boolean loop) {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.setLooping(loop);
        }
    }

    synchronized public void play() {
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.setLooping(true);
        }
    }

    synchronized public void loopedUpdate() {
        if(mMediaPlayer != null) {
            if(!mMediaPlayer.isPlaying())
                return;

            if(mMediaPlayer.getCurrentPosition() >= mEndTime || mMediaPlayer.getCurrentPosition() < mStartTime) {
                mMediaPlayer.seekTo(mStartTime, MediaPlayer.SEEK_CLOSEST);
            }
        }
    }

    // Returns true if song has finished playing
    synchronized public boolean updateOnce() {
        if(mMediaPlayer != null) {
            if(!mMediaPlayer.isPlaying())
                return true;

            return mMediaPlayer.getCurrentPosition() >= mEndTime && mMediaPlayer.getCurrentPosition() < mStartTime;
        }
        return false;
    }

    synchronized public void pause() {
        mMediaPlayer.pause();
    }

    synchronized public void seekTo(int millisec) {
        if(mMediaPlayer != null) {
            mMediaPlayer.seekTo(millisec);
            loopedUpdate();
        }
    }

    synchronized public void reset()
    {
        mMediaPlayer.reset();
    }

    // In milliseconds
    synchronized public int getDuration() {
        if(mMediaPlayer != null)
            return mMediaPlayer.getDuration();
        return 0;
    }

    synchronized public int getCurrentPosition() {
        if(mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        return 0;
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
    public void setEndTime(int t)  { mEndTime = t; loopedUpdate(); }

    synchronized public boolean isPlaying() {
        if(mMediaPlayer != null)
            return mMediaPlayer.isPlaying();
        return false;
    }
}
