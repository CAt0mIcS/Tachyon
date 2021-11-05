package com.mucify.objects;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mucify.Globals;
import com.mucify.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Song {
    static Song sInstance = null;

    private File mSongFilePath;
    protected File mLoopFilePath = null;
    private int mStartTime;
    private int mEndTime;

    private BroadcastReceiver mNoisyAudioReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
                pause();
        }
    };
    private IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    protected MediaPlayer mMediaPlayer;
    private Context mContext;

    public static Song get() {
        return sInstance;
    }

    // After song is started or repeated
    public interface MediaPlayerStartedListener {
        void onStarted(Song song);
    }
    // After song finished, also after repeat finished
    public interface MediaPlayerFinishedListener {
        void onFinished(Song song);
    }

    private ArrayList<MediaPlayerStartedListener> mMediaPlayerStartedListeners = new ArrayList<>();
    private ArrayList<MediaPlayerFinishedListener> mMediaPlayerFinishedListeners = new ArrayList<>();

    // path can be either a loop file or a song file
    public static Song create(Context context, File path) throws IOException {
        sInstance = new Song(context, path);
        return sInstance;
    }

    public static Song create(Context context, File songFilePath, int startTime, int endTime) throws IOException {
        sInstance = new Song(context, songFilePath, startTime, endTime);
        return sInstance;
    }

    // path can be either a loop file or a song file
    public Song(Context context, File path) throws IOException {
        createInternal(context, path);
    }

    public Song(Context context, File songFilePath, int startTime, int endTime) throws IOException {
        mStartTime = startTime;
        mEndTime = endTime;

        createInternal(context, songFilePath);
    }

    public boolean isSame(Song song) {
        return mSongFilePath == song.mSongFilePath && mStartTime == song.mStartTime && mEndTime == song.mEndTime;
    }

    protected void createInternal(Context context, File path) throws IOException {
        mContext = context;

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

        int currentPos = mMediaPlayer.getCurrentPosition();
        if(currentPos >= mEndTime || currentPos < mStartTime) {
            mContext.unregisterReceiver(mNoisyAudioReceiver);
            for(MediaPlayerFinishedListener listener : mMediaPlayerFinishedListeners) {
                listener.onFinished(this);
            }
        }
    }

    public void start() {
        mContext.registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);

        mMediaPlayer.start();
        // If MediaPlayer.SEEK_CLOSEST can't seek close enough, the song will "finish" at the beginning.
        // Threshold to prevent this
        seekTo(mStartTime + 20);

        for(MediaPlayerStartedListener listener : mMediaPlayerStartedListeners) {
            listener.onStarted(this);
        }
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

    public void addOnMediaPlayerStartedListener(@NonNull MediaPlayerStartedListener listener) {
        mMediaPlayerStartedListeners.add(listener);
    }

    public void addOnMediaPlayerFinishedListener(@NonNull MediaPlayerFinishedListener listener) {
        mMediaPlayerFinishedListeners.add(listener);
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            for(MediaPlayerFinishedListener lstnr : mMediaPlayerFinishedListeners)
                lstnr.onFinished(Song.this);
        });
    }

    public void addOnMediaPlayerStartedListener(int index, @NonNull MediaPlayerStartedListener listener) {
        mMediaPlayerStartedListeners.add(index, listener);
    }

    public void addOnMediaPlayerFinishedListener(int index, @NonNull MediaPlayerFinishedListener listener) {
        mMediaPlayerFinishedListeners.add(index, listener);
        mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
            for(MediaPlayerFinishedListener lstnr : mMediaPlayerFinishedListeners)
                lstnr.onFinished(Song.this);
        });
    }

    public static String toName(String file) {
        return file.replace(Utils.getFileExtension(file), "");
    }

    public static String toName(File file) {
        return toName(file.getName());
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
