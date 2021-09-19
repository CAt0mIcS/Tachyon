package com.mucify;

import android.app.AlertDialog;
import android.content.Context;

import java.util.Optional;

public class Utils {
    public static String getFileExtension(String filename) {
        Optional opt = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".")));

        if(opt.isPresent())
            return (String)opt.get();
        return "";
    }

    public static void messageBox(Context context, String title, String msg) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setMessage(msg);
        dlgAlert.setTitle(title);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}
