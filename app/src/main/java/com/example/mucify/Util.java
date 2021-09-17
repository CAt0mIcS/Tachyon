package com.example.mucify;

import android.app.AlertDialog;
import android.content.Context;

import java.util.Optional;

public class Util {
    public static Optional<String> getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));
    }

    public static void messageBox(Context context, String title, String msg) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(title);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
