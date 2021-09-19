package com.mucify.objects;

import android.content.Context;

import com.mucify.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Loop extends Song {
    private int mStartTime;
    private int mEndTime;
    private File mLoopFilePath;

    public Loop(Context context, File file) throws IOException {
        mLoopFilePath = file;
        if(!mLoopFilePath.exists())
            throw new IllegalArgumentException("File '" + mLoopFilePath.getPath() + "' doesn't exist");

        super.create(context, parseFile());
    }

    public static String toName(File file) {
        return file.getName().replace(Globals.LoopFileIdentifier, "").replace(Globals.LoopFileExtension, "");
    }

    // Returns path to song file
    private File parseFile() throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(mLoopFilePath));
            File file = new File(reader.readLine());
            mStartTime = Integer.parseInt(reader.readLine());
            mEndTime = Integer.parseInt(reader.readLine());
            reader.close();
            return file;
    }
}
