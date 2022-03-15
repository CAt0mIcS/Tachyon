package com.de.mucify.player;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.de.mucify.FileManager;
import com.de.mucify.MediaLibrary;
import com.de.mucify.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Song extends Playback {
    private String mTitle;
    private String mArtist;

    private MediaPlayer mMediaPlayer;

    private int mStartTime;
    private int mEndTime;

    private boolean mLooping = true;

    private File mSongFilePath;
    private File mLoopFilePath;


    public Song(Context context, File path) throws LoadingFailedException {
        this(path);
        create(context);
    }

    public Song(File path) throws LoadingFailedException {
        if(path == null || !path.exists()) {
            throw new LoadingFailedException("Failed to load song: \"" + path + "\" does not exist");
        }

        if(FileManager.isLoopFile(path)) {
            try {
                parseLoopFile(path);
            } catch (IOException e) {
                throw new LoadingFailedException("Failed to load loop: \"" + mSongFilePath + "\" is not a valid loop file");
            }
            mLoopFilePath = path;
        }
        else if(FileManager.isSongFile(path)) {
            mSongFilePath = path;
        }
        else {
            throw new LoadingFailedException("Failed to load song: \"" + mSongFilePath + "\" is an invalid file type");
        }

        if(!mSongFilePath.exists()) {
            throw new LoadingFailedException("Failed to load song: \"" + mSongFilePath + "\" does not exist");
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

    public Song(Context context, File songFilePath, int startTime, int endTime) throws LoadingFailedException {
        this(context, songFilePath);
        mStartTime = startTime;
        mEndTime = endTime;
    }

    @Override
    public boolean isPlaying() { return mMediaPlayer.isPlaying(); }

    @Override
    public void seekTo(int millis) {
        mMediaPlayer.seekTo(millis);
    }

    @Override
    public void start() {
        mMediaPlayer.start();

        if(mCallback != null)
            mCallback.onPlayPause(false);
        Log.d("Mucify.Song", "Song.start");
    }

    @Override
    public void restart() {
        mMediaPlayer.seekTo(mStartTime);
        mMediaPlayer.start();

        if(mCallback != null)
            mCallback.onPlayPause(false);
        Log.d("Mucify.Song", "Song.restart");
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();

        if(mCallback != null)
            mCallback.onPlayPause(true);
        Log.d("Mucify.Song", "Song.pause");
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
        Log.d("Mucify.Song", "Song.stop");
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
        Log.d("Mucify.Song", "Song.reset");
    }

    @Override
    public void create(Context context) {
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));
        mMediaPlayer.setLooping(mLooping);

        if(mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();

        Log.d("Mucify.Song", "Song.create");
    }

    @Override
    public String getMediaId() {
        if(isLoop())
            return getLoopMediaId(this);
        return getSongMediaId(this);
    }

    public static String getSongMediaId(Song song) {
        return "Song_" + MediaLibrary.getSongIndex(song);
    }

    public static String getLoopMediaId(Song song) {
        return "Loop_" + MediaLibrary.getLoopIndex(song);
    }

    @Override
    public Song next() {
        Log.d("Mucify.Song", "Song.next");

        if(!isLoop()) {
            int idx = MediaLibrary.getSongIndex(this) + 1;
            if(idx >= MediaLibrary.AvailableSongs.size())
                idx = 0;
            return MediaLibrary.AvailableSongs.get(idx);
        }
        else {
            int idx = MediaLibrary.getLoopIndex(this) + 1;
            if(idx >= MediaLibrary.AvailableLoops.size())
                idx = 0;
            return MediaLibrary.AvailableLoops.get(idx);
        }
    }

    @Override
    public Song previous() {
        Log.d("Mucify.Song", "Song.previous");

        if(!isLoop()) {
            int idx = MediaLibrary.getSongIndex(this) - 1;
            if(idx < 0)
                idx = MediaLibrary.AvailableSongs.size() - 1;
            return MediaLibrary.AvailableSongs.get(idx);
        }
        else {
            int idx = MediaLibrary.getLoopIndex(this) - 1;
            if(idx < 0)
                idx = MediaLibrary.AvailableLoops.size() - 1;
            return MediaLibrary.AvailableLoops.get(idx);
        }
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSubtitle() {
        return getArtist();
    }

    public String getArtist() { return mArtist; }
    public File getSongPath() { return mSongFilePath; }
    public void setStartTime(int startTime) { mStartTime = startTime; }
    public void setEndTime(int endTime) { mEndTime = endTime; }
    public int getStartTime() { return mStartTime; }
    public int getEndTime() { return mEndTime; }
    public File getLoopPath() { return mLoopFilePath; }
    public String getLoopName() { return FileManager.loopNameFromFile(mLoopFilePath); }
    public void setVolume(float left, float right) { mMediaPlayer.setVolume(left, right); }
    public boolean isLoop() { return mLoopFilePath != null; }
    public void setLooping(boolean looping) { if(isCreated()) mMediaPlayer.setLooping(looping); else mLooping = looping; }
    public void setOnMediaPlayerCompletionListener(MediaPlayer.OnCompletionListener listener) { mMediaPlayer.setOnCompletionListener(listener); }
    public boolean isCreated() { return mMediaPlayer != null; }
    public boolean isLooping() { return mLooping; }

    public boolean equalsUninitialized(@Nullable Song obj) {
        if(obj == null)
            return false;
        boolean equals = mTitle.equals(obj.mTitle) && mArtist.equals(obj.mArtist) && mStartTime == obj.mStartTime && mSongFilePath.equals(obj.mSongFilePath);
        if(equals && mLoopFilePath != null && obj.mLoopFilePath != null)
            equals = mLoopFilePath.equals(obj.mLoopFilePath);
        else if(mLoopFilePath == null && obj.mLoopFilePath == null) {}
        else equals = false;
        return equals;
    }

    public boolean equals(@Nullable Song obj) {
        if(obj == null)
            return false;
        boolean equals = mTitle.equals(obj.mTitle) && mArtist.equals(obj.mArtist) && mStartTime == obj.mStartTime && mEndTime == obj.mEndTime && mMediaPlayer == obj.mMediaPlayer && mSongFilePath.equals(obj.mSongFilePath);
        if(equals && mLoopFilePath != null && obj.mLoopFilePath != null)
            equals = mLoopFilePath.equals(obj.mLoopFilePath);
        else if(mLoopFilePath == null && obj.mLoopFilePath == null) {}
        else equals = false;
        return equals;
    }

    private void parseLoopFile(File file) throws IOException {
        Log.d("Mucify.Song", "Parsing loop file " + file);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        mSongFilePath = new File(reader.readLine());
        mStartTime = Integer.parseInt(reader.readLine());
        mEndTime = Integer.parseInt(reader.readLine());
        reader.close();
    }


    public class LoadingFailedException extends Exception {
        public LoadingFailedException(String msg) {
            super(msg);
        }
    }
}
