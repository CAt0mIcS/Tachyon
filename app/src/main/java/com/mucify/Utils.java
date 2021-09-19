package com.mucify;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

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
}
