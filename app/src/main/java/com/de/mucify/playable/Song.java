package com.de.mucify.playable;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import com.de.mucify.utils.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Song {

    private String mTitle;
    private String mArtist;

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    private int mStartTime;
    private int mEndTime;

    private File mSongFilePath;
    private File mLoopFilePath;

    /**
     * @param  path path to either a song audio file or a loop file
     */
    public Song(Context context, File path) {
        create(context, path);
    }

    public Song(Context context, File songFilePath, int startTime, int endTime) {
        mStartTime = startTime;
        mEndTime = endTime;

        create(context, songFilePath);
    }

    public void reset() { mMediaPlayer.reset(); }

    public void start() {
        mMediaPlayer.start();
        // If MediaPlayer.SEEK_CLOSEST can't seek close enough, the song will "finish" at the beginning.
        // Threshold to prevent this
        if(mStartTime != 0)
            seekTo(mStartTime + 20);
    }

    public void seekTo(int milliseconds) {
        mMediaPlayer.seekTo(milliseconds, MediaPlayer.SEEK_CLOSEST);
    }

    public String getTitle() { return mTitle; }
    public String getArtist() { return mArtist; }


    private void create(Context context, File path) {
        if(!path.exists()) {
            // MY_TODO: Add error message for user
            return;
        }

        if(FileManager.isLoopFile(path)) {
            try {
                parseLoopFile(path);
            } catch (IOException e) {
                // MY_TODO: Add error message for invalid file (format)
                return;
            }
            mLoopFilePath = path;
        }
        else if(FileManager.isSongFile(path)) {
            mSongFilePath = path;
        }
        else
            return;  // MY_TODO: Add invalid file type error message for user

        if(!mSongFilePath.exists()) {
            return;
            // MY_TODO: Add error message for user
        }
        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));

        if(mEndTime == 0)
            mEndTime = mMediaPlayer.getDuration();

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
        if(title != null)
            mTitle = title;
    }

    private void parseLoopFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        mSongFilePath = new File(reader.readLine());
        mStartTime = Integer.parseInt(reader.readLine());
        mEndTime = Integer.parseInt(reader.readLine());
        reader.close();
    }
}
