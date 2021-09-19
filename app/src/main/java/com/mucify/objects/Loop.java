package com.mucify.objects;

import android.content.Context;

import com.mucify.Globals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Loop {
    public static String toName(File file) {
        return file.getName().replace(Globals.LoopFileIdentifier, "").replace(Globals.LoopFileExtension, "").replace("_", " | ");
    }

    public static File toFile(String songName, String name) {
        return new File(Globals.DataDirectory + "/" + Globals.LoopFileIdentifier + songName + "_" + name + Globals.LoopFileExtension);
    }

    public static void save(Song song, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(song.getPath().getPath() + '\n');
        writer.write(String.valueOf(song.getStartTime()) + '\n');
        writer.write(String.valueOf(song.getEndTime()) + '\n');
        writer.close();
    }
}
