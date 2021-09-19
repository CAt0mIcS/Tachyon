package com.mucify.objects;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.mucify.Globals;
import com.mucify.Utils;

import java.io.File;

public class Song {
    private File mSongFilePath;

    protected MediaPlayer mMediaPlayer;

    public Song(Context context, File songFilePath) {
        create(context, songFilePath);
    }

    protected Song() {}

    protected void create(Context context, File songFilePath) {
        mSongFilePath = songFilePath;

        if(!mSongFilePath.exists())
            throw new IllegalArgumentException("File '" + mSongFilePath.getPath() + "' doesn't exist");

        mMediaPlayer = MediaPlayer.create(context, Uri.parse(mSongFilePath.getPath()));
    }

    public String getName() {
        return Song.toName(mSongFilePath);
    }

    public static String toName(File file) {
        return file.getName().replace(Utils.getFileExtension(file.getName()), "");
    }
}
