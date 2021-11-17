package com.de.mucify.playable;

import android.content.IntentFilter;
import android.media.AudioManager;

import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.util.FileManager;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractCollection;
import java.util.ArrayList;

public class AudioController {
    private static final AudioController sInstance = new AudioController();
    private Song mSong;
    private boolean mIsSongPaused = false;

    public static final int INDEX_DONT_CARE = -1;

    private final ArrayList<SongResetListener> mSongResetListeners = new ArrayList<>();
    private final ArrayList<SongPausedListener> mSongPausedListeners = new ArrayList<>();
    private final ArrayList<SongUnpausedListener> mSongUnpausedListeners = new ArrayList<>();
    private final ArrayList<SongStartedListener> mSongStartedListeners = new ArrayList<>();
    private final ArrayList<SongFinishedListener> mSongFinishedListeners = new ArrayList<>();

    public static AudioController get() { return sInstance; }

    private AudioController() {
        new Thread(() -> {
            while(true) {
                // MY_TODO: If song is reset after !isSongNull is true, we'll get a NullPointerException
                // Try/catch is only temporary
                if(!isSongNull()) {
                    try {
                        int currentPos = getCurrentSongPosition();
                        if(currentPos >= getSongEndTime() || currentPos < getSongStartTime() || (!mSong.isPlaying() && !isPaused())) {
                            for(SongFinishedListener listener : mSongFinishedListeners) {
                                listener.onFinished(mSong);
                            }

                            mSong.start();
                            if(isPaused())
                                mSong.pause();
                        }
                    } catch(NullPointerException ignored) {}

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
            mSong = null;
        }
    }

    public void setSong(Song song) {
        if(mSong != null)
            mSong.reset();
        setSongNoReset(song);
    }

    public void setLooping(boolean looping) { mSong.setLooping(looping); }

    public void setSongNoReset(Song song) {
        mSong = song;
    }

    synchronized public void startSong() {
        mSong.start();
        if(mIsSongPaused) {
            for(SongUnpausedListener listener : mSongUnpausedListeners)
                listener.onUnpause(mSong);
            mIsSongPaused = false;
        }
        else
            for(SongStartedListener listener : mSongStartedListeners)
                listener.onStarted(mSong);
    }

    public boolean isSongPlaying() { return mSong.isPlaying(); }
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
    public boolean isPaused() { return mIsSongPaused; }
    public Song getSong() { return mSong; }


    public void pauseSong() {
        if(!isPaused()) {
            mSong.pause();
            mIsSongPaused = true;
            for(SongPausedListener listener : mSongPausedListeners)
                listener.onPause(mSong);
        }
    }

    public void unpauseSong() {
        if(isPaused()) {
            mSong.unpause();
            mIsSongPaused = false;
            for(SongUnpausedListener listener : mSongUnpausedListeners)
                listener.onUnpause(mSong);
        }
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
    public void addOnSongFinishedListener(SongFinishedListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongFinishedListeners.add(i, listener);
        else
            mSongFinishedListeners.add(listener);
    }

    public void saveAsLoop(String loopName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FileManager.loopNameToFile(loopName)));
        writer.write(mSong.getSongPath().getPath() + '\n');
        writer.write(String.valueOf(getSongStartTime()) + '\n');
        writer.write(String.valueOf(getSongEndTime()) + '\n');
        writer.close();
    }

    public void addOnSongStartedListener(SongStartedListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongStartedListeners.add(i, listener);
        else
            mSongStartedListeners.add(listener);
    }

    public interface SongStartedListener {
        void onStarted(Song song);
    }
    public interface SongFinishedListener {
        void onFinished(Song song);
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
