package com.de.mucify.playable;

import android.media.MediaPlayer;

import com.de.mucify.util.UserSettings;

import java.util.ArrayList;

public class AudioController {
    private static final AudioController sInstance = new AudioController();
    private Song mSong;
    private boolean mWasSongPaused = false;


    public static final int INDEX_DONT_CARE = -1;

    private final ArrayList<SongResetListener> mSongResetListeners = new ArrayList<>();
    private final ArrayList<SongPausedListener> mSongPausedListeners = new ArrayList<>();
    private final ArrayList<SongUnpausedListener> mSongUnpausedListeners = new ArrayList<>();

    public static AudioController get() { return sInstance; }

    private AudioController() {
        new Thread(() -> {
            while(true) {
                if(!isSongNull()) {
                    int currentPos = getCurrentSongPosition();
                    if(currentPos >= getSongEndTime() || currentPos < getSongStartTime()) {
                        mSong.start();
                        if(mWasSongPaused)
                            mSong.pause();
                    }
                }

                try {
                    // MY_TODO: Not thread safe (User changes setting while reading here)
                    Thread.sleep(UserSettings.AudioUpdateInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void reset() {
        if(mSong != null) {
            for(SongResetListener listener : mSongResetListeners)
                listener.onReset(mSong);
            mSong.reset();
        }
    }

    public void setSong(Song song) {
        if(mSong != null)
            mSong.reset();
        mSong = song;
    }

    synchronized public void startSong() { mSong.start(); }
    public boolean isSongPlaying() {
        return mSong.isPlaying();
    }
    public int getCurrentSongPosition() { return mSong.getCurrentPosition(); }
    public boolean isSongNull() { return mSong == null; }
    public int getSongDuration() { return mSong.getDuration(); }
    public String getSongTitle() { return mSong.getTitle(); }
    public String getSongArtist() { return mSong.getArtist(); }
    public void seekSongTo(int milliseconds) { mSong.seekTo(milliseconds); }
    public void setSongStartTime(int startTime) { mSong.setStartTime(startTime); }
    public void setSongEndTime(int endTime) { mSong.setEndTime(endTime); }
    public int getSongStartTime() { return mSong.getStartTime(); }
    public int getSongEndTime() { return mSong.getEndTime(); }

    public void pauseSong() {
        mSong.pause();
        mWasSongPaused = true;
        for(SongPausedListener listener : mSongPausedListeners)
            listener.onPause(mSong);
    }

    public void unpauseSong() {
        mSong.unpause();
        mWasSongPaused = false;
        for(SongUnpausedListener listener : mSongUnpausedListeners)
            listener.onUnpause(mSong);
    }

    public void addOnSongResetListener(SongResetListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongResetListeners.add(i, listener);
        else
            mSongResetListeners.add(listener);
    }
    public void addOnSongPausedListener(SongPausedListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongPausedListeners.add(i, listener);
        else
            mSongPausedListeners.add(listener);
    }
    public void addOnSongUnpausedListener(SongUnpausedListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongUnpausedListeners.add(i, listener);
        else
            mSongUnpausedListeners.add(listener);
    }

    public interface SongResetListener {
        void onReset(Song song);
    }
    public interface SongPausedListener {
        void onPause(Song song);
    }
    public interface SongUnpausedListener {
        void onUnpause(Song song);
    }

}
