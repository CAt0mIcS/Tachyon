package com.de.mucify.playable;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;

public class Song {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    public Song(Context context, String songFilePath) {
        mContext = context;

        mMediaPlayer = MediaPlayer.create(context, Uri.parse(songFilePath));
        mMediaPlayer.start();
    }

    public void reset() {
        mMediaPlayer.reset();
    }
}
