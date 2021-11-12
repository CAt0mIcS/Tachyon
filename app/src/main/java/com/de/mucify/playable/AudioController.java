package com.de.mucify.playable;

import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.util.UserSettings;
import com.de.mucify.util.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class AudioController {
    private static final AudioController sInstance = new AudioController();
    private Song mSong;
    private boolean mIsSongPaused = false;

    public static final int INDEX_DONT_CARE = -1;

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private final AudioFocusRequest mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            .setAcceptsDelayedFocusGain(false)
            .setOnAudioFocusChangeListener(this::onAudioFocusChange)
            .build();
    private boolean mPlaybackAuthorized = false;

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
                        if(mIsSongPaused)
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

        addOnSongResetListener(song -> unregisterNoisy(), INDEX_DONT_CARE);
        addOnSongUnpausedListener(song -> {
            if(!mPlaybackAuthorized && !requestAudioFocus())
                pauseSong();
        }, INDEX_DONT_CARE);
    }

    public void reset() {
        if(mSong != null) {
            for(SongResetListener listener : mSongResetListeners)
                listener.onReset(mSong);
            mSong.reset();
            mSong = null;

            getAudioManager().abandonAudioFocusRequest(mAudioFocusRequest);
            mPlaybackAuthorized = false;
        }
    }

    public void setSong(Song song) {
        if(mSong != null)
            mSong.reset();
        mSong = song;
    }

    synchronized public void startSong() {
        if(requestAudioFocus()) {
            mSong.start();
            registerNoisy();
        }
        else
            pauseSong();
    }
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
        mIsSongPaused = true;
        for(SongPausedListener listener : mSongPausedListeners)
            listener.onPause(mSong);
    }

    public void unpauseSong() {
        mSong.unpause();
        mIsSongPaused = false;
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

    public void saveAsLoop(String loopName) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(Utils.loopNameToFile(loopName)));
        writer.write(mSong.getSongPath().getPath() + '\n');
        writer.write(String.valueOf(getSongStartTime()) + '\n');
        writer.write(String.valueOf(getSongEndTime()) + '\n');
        writer.close();
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


    private void registerNoisy() {
        SingleAudioPlayActivity.get().registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);
    }

    private boolean requestAudioFocus() {
        int result = getAudioManager().requestAudioFocus(mAudioFocusRequest);

        switch(result) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
            case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
                mPlaybackAuthorized = false;
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                mPlaybackAuthorized = true;
                break;
        }

        return mPlaybackAuthorized;
    }

    private void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mPlaybackAuthorized = true;
                unpauseSong();
                mSong.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:  // focus loss for long time: should free resources
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: // focus loss for short time
                mPlaybackAuthorized = false;
                pauseSong();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mSong.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void unregisterNoisy() {
        SingleAudioPlayActivity.get().unregisterReceiver(mNoisyAudioReceiver);
    }

    private AudioManager getAudioManager() { return (AudioManager)SingleAudioPlayActivity.get().getSystemService(Context.AUDIO_SERVICE); }

}
