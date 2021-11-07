package com.de.mucify.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.de.mucify.R;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.receiver.ForegroundNotificationClickReceiver;

import java.io.File;

public class SongPlayForegroundService extends IntentService {
    static SongPlayForegroundService sInstance = null;

    private static final int NOTIFY_ID = 1337;
    private static final int FOREGROUND_ID = 1338;
    private static final int SERVICE_ID = 1339;

    private final Object mMutex = new Object();

    public SongPlayForegroundService() {
        super("com.de.mucify.SongPlayForegroundService");

        if(sInstance != null)
            sInstance.reset();

        sInstance = this;
    }

    public void reset() {
        AudioController.get().reset();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        synchronized (mMutex) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startCustomForegroundService(R.drawable.ic_black_pause);
//            else
//                startForeground(1, new Notification());

            AudioController.get().addOnSongResetListener(song -> {
                stopForeground(true);
                synchronized (mMutex) {
                    mMutex.notify();
                }
            }, 0);
            AudioController.get().addOnSongPausedListener(song -> {
//                stopForeground(false);  // MY_TODO: Let user swipe notification away
//                synchronized (mMutex) {
//                    mMutex.notify();
//                }

//                stopForeground(true);
                startCustomForegroundService(R.drawable.ic_black_play);
            }, 0);
            AudioController.get().addOnSongUnpausedListener(song -> {
//                stopForeground(true);
                startCustomForegroundService(R.drawable.ic_black_pause);
            }, 0);

            // Wait until song reset/paused
            try {
                mMutex.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sInstance = null;
    }

    public static SongPlayForegroundService get() { return sInstance; }

    private void startCustomForegroundService(@DrawableRes int playPauseIconID) {
        String NOTIFICATION_CHANNEL_ID = "com.mucify";
        String channelName = "Music playing background service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        // Get the layouts to use in the custom notification
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.song_loop_playing_foreground_notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.song_loop_playing_foreground_notification_large);

        notificationLayout.setTextViewText(R.id.notification_txtTitle, AudioController.get().getSongTitle());
        notificationLayout.setTextViewText(R.id.notification_txtArtist, AudioController.get().getSongArtist());

        notificationLayout.setTextColor(R.id.notification_txtTitle, ResourcesCompat.getColor(getResources(), R.color.black_text_color, null));
        notificationLayout.setTextColor(R.id.notification_txtArtist, ResourcesCompat.getColor(getResources(), R.color.black_secondary_text_color, null));

        notificationLayout.setImageViewResource(R.id.notification_btnPlayPause, playPauseIconID);

        ForegroundNotificationClickReceiver receiver = new ForegroundNotificationClickReceiver();
        IntentFilter filter = new IntentFilter("com.de.mucify.PLAY_PAUSE");
        filter.addAction("com.de.mucify.FOREGROUND_CLOSE");
        registerReceiver(receiver, filter);

        Intent pauseIntent = new Intent("com.de.mucify.PLAY_PAUSE");
        pauseIntent.putExtra("PlayPause", playPauseIconID);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 12345, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.notification_btnPlayPause, pendingIntentPause);

        Intent closeIntent = new Intent("com.de.mucify.FOREGROUND_CLOSE");
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(this, 12345, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.notification_btnRemove, pendingIntentClose);

        // Apply the layouts to the notification
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
//                .addAction(R.drawable.ic_black_play, "Yes", pendingIntentYes)
//                .setCustomBigContentView(notificationLayoutExpanded)
                .setColorized(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.audio_playing_notification_background, null))
                .build();

        startForeground(NOTIFY_ID, notification);
    }
}