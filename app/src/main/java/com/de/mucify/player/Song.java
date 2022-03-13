package com.de.mucify.player;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;

import com.de.mucify.FileManager;

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
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));
        mMediaPlayer.setLooping(mLooping);

        if(mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();
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
    public String getLoopName() { return FileManager.loopNameFromFile(mLoopFilePath); }
    public void setVolume(float left, float right) { mMediaPlayer.setVolume(left, right); }
    public boolean isLoop() { return mLoopFilePath != null; }
    public void setLooping(boolean looping) { if(isCreated()) mMediaPlayer.setLooping(looping); else mLooping = looping; }
    public void setOnMediaPlayerCompletionListener(MediaPlayer.OnCompletionListener listener) { mMediaPlayer.setOnCompletionListener(listener); }
    public boolean isCreated() { return mMediaPlayer != null; }
    public boolean isLooping() { return mLooping; }

    private void parseLoopFile(File file) throws IOException {
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
