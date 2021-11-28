package com.de.mucify.playable;

import android.content.Context;

import com.de.mucify.util.FileManager;
import com.de.mucify.util.MediaLibrary;
import com.de.mucify.util.UserSettings;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AudioController {
    private static final AudioController sInstance = new AudioController();
    private Song mSong;
    private Playlist mPlaylist;
    private boolean mIsSongPaused = false;

    public static final int INDEX_DONT_CARE = -1;

    private final ArrayList<SongResetListener> mSongResetListeners = new ArrayList<>();
    private final ArrayList<SongPausedListener> mSongPausedListeners = new ArrayList<>();
    private final ArrayList<SongUnpausedListener> mSongUnpausedListeners = new ArrayList<>();
    private final ArrayList<SongStartedListener> mSongStartedListeners = new ArrayList<>();
    private final ArrayList<OnSongSeekedListener> mSongSeekedListeners = new ArrayList<>();
    private final ArrayList<NextSongListener> mNextSongListeners = new ArrayList<>();

    public static AudioController get() { return sInstance; }

    private AudioController() {
        new Thread(() -> {
            while(true) {
                // MY_TODO: If song is reset after !isSongNull is true, we'll get a NullPointerException
                // Try/catch is only temporary
                try {
                    if(!isSongNull() && mSong.isCreated() && mSong.isPlaying()) {
                        int currentPos = getCurrentSongPosition();
                        if(currentPos >= getSongEndTime() || currentPos < getSongStartTime() || (!mSong.isPlaying() && !isPaused())) {
                            if(!isPlaylistNull()) {
                                playlistNext();
                            }
                            else {
                                mSong.start();
                                for(OnSongSeekedListener listener : mSongSeekedListeners)
                                    listener.onSeeked(mSong);
                            }
                            if(isPaused())
                                mSong.pause();

                        }
                    }
                } catch(NullPointerException ignored) {}

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
        if(mPlaylist != null) {
            mPlaylist.reset();
            mPlaylist = null;
        }
    }

    public void setSong(Song song) {
        if(mSong != null)
            mSong.reset();
        if(mPlaylist != null) {
            mPlaylist.reset();
            mPlaylist = null;
        }
        mSong = song;
    }

    public void setPlaylist(Playlist playlist) {
        if(mPlaylist != null)
            mPlaylist.reset();
        if(mSong != null)
            mSong.reset();
        mPlaylist = playlist;
        mSong = mPlaylist.getPlayingSongs().get(0);
        mSong.create(mPlaylist.getContext());
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

    public void next(Context context) {
        if(!isPlaylistNull())
            playlistNext();
        else
            songNext(context);
    }

    public void previous(Context context) {
        if(!isPlaylistNull())
            playlistPrevious();
        else
            songPrevious(context);
    }

    public boolean isSongPlaying() { return mSong.isPlaying(); }
    public int getCurrentSongPosition() { return mSong.getCurrentPosition(); }
    public boolean isSongNull() { return mSong == null; }
    public int getSongDuration() { return mSong.getDuration(); }
    public String getSongTitle() { return mSong.getTitle(); }
    public String getSongArtist() { return mSong.getArtist(); }
    public void seekSongTo(long milliseconds) { mSong.seekTo(milliseconds); for(OnSongSeekedListener listener : mSongSeekedListeners) listener.onSeeked(mSong); }
    public void setSongStartTime(int startTime) { mSong.setStartTime(startTime); }
    public void setSongEndTime(int endTime) { mSong.setEndTime(endTime); }
    public int getSongStartTime() { return mSong.getStartTime(); }
    public int getSongEndTime() { return mSong.getEndTime(); }
    public boolean isPaused() { return mIsSongPaused; }
    public Song getSong() { return mSong; }
    public Playlist getPlaylist() { return mPlaylist; }
    public boolean isPlaylistNull() { return mPlaylist == null; }


    public void pauseSong() {
        if(!isPaused()) {
            pauseSongInternal();
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
    public void addOnSongSeekedListener(OnSongSeekedListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mSongSeekedListeners.add(i, listener);
        else
            mSongSeekedListeners.add(listener);
    }
    public void addOnNextSongListener(NextSongListener listener, int i) {
        if(i != INDEX_DONT_CARE)
            mNextSongListeners.add(i, listener);
        else
            mNextSongListeners.add(listener);
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
    public interface OnSongSeekedListener {
        void onSeeked(Song song);
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
    public interface NextSongListener {
        void onNextSong(Song nextSong);
    }


    private void pauseSongInternal() {
        mSong.pause();
        mIsSongPaused = true;
        for(SongPausedListener listener : mSongPausedListeners)
            listener.onPause(mSong);
    }

    private void playlistNext() {
        mSong = mPlaylist.next();
        mSong.start();

        if(mIsSongPaused)
            pauseSongInternal();

        for(NextSongListener listener : mNextSongListeners)
            listener.onNextSong(mSong);
    }

    private void playlistPrevious() {
        Song previous = mPlaylist.previous();
        if(previous == null)
            return;
        mSong = previous;
        mSong.start();

        if(mIsSongPaused)
            pauseSongInternal();

        for(NextSongListener listener : mNextSongListeners)
            listener.onNextSong(mSong);
    }

    private void songNext(Context context) {
        // MY_TODO: Fix next being called too often
        if(mSong.isLoop()) {
            for(int i = 0; i < MediaLibrary.AvailableLoops.size(); ++i) {
                Song loop = MediaLibrary.AvailableLoops.get(i);
                if(loop.equalsUninitialized(mSong)) {
                    int songIndex = i + 1;
                    if(songIndex >= MediaLibrary.AvailableLoops.size())
                        songIndex = 0;
                    mSong.reset();
                    mSong = MediaLibrary.AvailableLoops.get(songIndex);
                    mSong.create(context);
                    mSong.start();
                    if(mIsSongPaused)
                        pauseSongInternal();

                    for(NextSongListener listener : mNextSongListeners)
                        listener.onNextSong(mSong);

                    break;
                }
            }
        }
        else {
            for(int i = 0; i < MediaLibrary.AvailableSongs.size(); ++i) {
                Song song = MediaLibrary.AvailableSongs.get(i);
                if(song.equalsUninitialized(mSong)) {
                    int songIndex = i + 1;
                    if(songIndex >= MediaLibrary.AvailableSongs.size())
                        songIndex = 0;
                    mSong.reset();
                    mSong = MediaLibrary.AvailableSongs.get(songIndex);
                    mSong.create(context);
                    mSong.start();
                    if(mIsSongPaused)
                        pauseSongInternal();

                    for(NextSongListener listener : mNextSongListeners)
                        listener.onNextSong(mSong);

                    break;
                }
            }
        }
    }

    private void songPrevious(Context context) {
        // MY_TODO: Fix previous being called too often
        if(mSong.isLoop()) {
            for(int i = 0; i < MediaLibrary.AvailableLoops.size(); ++i) {
                Song loop = MediaLibrary.AvailableLoops.get(i);
                if(loop.equalsUninitialized(mSong)) {
                    int songIndex = i - 1;
                    if(songIndex < 0)
                        songIndex = MediaLibrary.AvailableLoops.size() - 1;
                    mSong.reset();
                    mSong = MediaLibrary.AvailableLoops.get(songIndex);
                    mSong.create(context);
                    mSong.start();
                    if(mIsSongPaused)
                        mSong.pause();

                    for(NextSongListener listener : mNextSongListeners)
                        listener.onNextSong(mSong);

                    break;
                }
            }
        }
        else {
            for(int i = 0; i < MediaLibrary.AvailableSongs.size(); ++i) {
                Song song = MediaLibrary.AvailableSongs.get(i);
                if(song.equalsUninitialized(mSong)) {
                    int songIndex = i - 1;
                    if(songIndex < 0)
                        songIndex = MediaLibrary.AvailableSongs.size() - 1;
                    mSong.reset();
                    mSong = MediaLibrary.AvailableSongs.get(songIndex);
                    mSong.create(context);
                    mSong.start();
                    if(mIsSongPaused)
                        mSong.pause();

                    for(NextSongListener listener : mNextSongListeners)
                        listener.onNextSong(mSong);

                    break;
                }
            }
        }
    }
}
