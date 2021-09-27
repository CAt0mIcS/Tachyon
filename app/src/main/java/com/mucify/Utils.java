package com.mucify;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.File;
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

    public static int getItemHeightOfListView(ListView listView, int items) {
        ListAdapter adapter = listView.getAdapter();
        final int UNBOUNDED = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        int grossElementHeight = 0;
        for (int i = 0; i < items; i++) {
            View childView = adapter.getView(i, null, listView);
            childView.measure(UNBOUNDED, UNBOUNDED);
            grossElementHeight += childView.getMeasuredHeight();
        }
        return grossElementHeight;
    }

    public static String millisecondsToReadableString(int progress) {
        long millis = progress % 1000;
        long second = (progress / 1000) % 60;
        long minute = (progress / (1000 * 60)) % 60;
        long hour = (progress / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
    }

    public static boolean isLoopFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(Globals.LoopFileExtension) && path.getName().indexOf(Globals.LoopFileIdentifier) == 0;
    }

    public static boolean isSongFile(File path) {
        return path.isFile() && Globals.SupportedAudioExtensions.contains(getFileExtension(path.getName()));
    }

    public static boolean isPlaylistFile(File path) {
        return path.isFile() && getFileExtension(path.getName()).equals(Globals.PlaylistFileExtension) && path.getName().indexOf(Globals.PlaylistFileIdentifier) == 0;
    }

    public static View getViewByPosition(ListView listView, int pos) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}
