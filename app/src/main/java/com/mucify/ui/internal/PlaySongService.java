package com.mucify.ui.internal;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mucify.R;
import com.mucify.objects.Song;

public class PlaySongService extends IntentService {
    private static final int NOTIFY_ID = 1337;
    private static final int FOREGROUND_ID = 1338;

    private final Object mMutex = new Object();

    public PlaySongService() {
        super("PlaySongService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null)
            return;

        synchronized (mMutex) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startCustomForegroundService();
            else
                startForeground(1, new Notification());

            Song.get().addOnMediaPlayerStoppedListener(0, song1 -> {
                stopForeground(true);
                synchronized (mMutex) {
                    mMutex.notify();
                }
            });
            try {
                mMutex.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void startCustomForegroundService() {
        String NOTIFICATION_CHANNEL_ID = "com.mucify";
        String channelName = "Music playing background service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_play_pause_btn)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }
}
