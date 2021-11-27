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
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.receiver.ForegroundNotificationClickReceiver;

import java.io.File;

public class SongPlayForegroundService extends IntentService {
    static SongPlayForegroundService sInstance = null;

    private final ForegroundNotificationClickReceiver mNotificationReceiver = new ForegroundNotificationClickReceiver();

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private static final int NOTIFY_ID = 1337;
    private boolean mAlreadyReset = false;

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
        if(AudioController.get().isSongNull() || mAlreadyReset)
            return;

        if(!startCustomForegroundService(R.drawable.ic_black_pause))
            return;

        registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);

        AudioController.get().addOnSongResetListener(song -> {
            stopForeground(true);
            mAlreadyReset = true;
            synchronized (mMutex) {
                mMutex.notify();
            }
        }, 0);
        AudioController.get().addOnSongPausedListener(song -> {
            startCustomForegroundService(R.drawable.ic_black_play);
        }, 0);
        AudioController.get().addOnSongUnpausedListener(song -> {
            startCustomForegroundService(R.drawable.ic_black_pause);
        }, 0);

        synchronized (mMutex) {
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
        try {
            unregisterReceiver(mNotificationReceiver);
            unregisterReceiver(mNoisyAudioReceiver);
        } catch(IllegalArgumentException ignored) {}
        sInstance = null;
    }

    public static SongPlayForegroundService get() { return sInstance; }

    private boolean startCustomForegroundService(@DrawableRes int playPauseIconID) {
        String NOTIFICATION_CHANNEL_ID = "com.mucify";
        String channelName = "Music playing background service";
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setLightColor(Color.BLUE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        // Get the layouts to use in the custom notification
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.song_loop_playing_foreground_notification_small);
//        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.song_loop_playing_foreground_notification_large);

        notificationLayout.setTextViewText(R.id.notification_txtTitle, AudioController.get().getSongTitle());
        notificationLayout.setTextViewText(R.id.notification_txtArtist, AudioController.get().getSongArtist());

        notificationLayout.setTextColor(R.id.notification_txtTitle, ResourcesCompat.getColor(getResources(), R.color.black_text_color, null));
        notificationLayout.setTextColor(R.id.notification_txtArtist, ResourcesCompat.getColor(getResources(), R.color.black_secondary_text_color, null));

        notificationLayout.setImageViewResource(R.id.notification_btnPlayPause, playPauseIconID);

        IntentFilter filter = new IntentFilter("com.de.mucify.PLAY_PAUSE");
        filter.addAction("com.de.mucify.FOREGROUND_CLOSE");
        registerReceiver(mNotificationReceiver, filter);

        Intent pauseIntent = new Intent("com.de.mucify.PLAY_PAUSE");
        pauseIntent.putExtra("PlayPause", playPauseIconID);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 12345, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.notification_btnPlayPause, pendingIntentPause);

        Intent closeIntent = new Intent("com.de.mucify.FOREGROUND_CLOSE");
        PendingIntent pendingIntentClose = PendingIntent.getBroadcast(this, 12345, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(R.id.notification_btnRemove, pendingIntentClose);

        Intent clickIntent = new Intent(this, SingleAudioPlayActivity.class);
        clickIntent.putExtra("PreserveSong", true);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(this, 0, clickIntent, 0);

        // Apply the layouts to the notification
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
//                .setCustomBigContentView(notificationLayoutExpanded)
                .setContentIntent(pendingClickIntent)
                .setColorized(true)
//                .setProgress(AudioController.get().getSongDuration(), AudioController.get().getCurrentSongPosition(), false)
//                .setCategory(Notification.CATEGORY_SERVICE)  // MY_TODO: Check which category fits needs https://developer.android.com/reference/androidx/core/app/NotificationCompat#CATEGORY_ALARM
                .setColor(ResourcesCompat.getColor(getResources(), R.color.audio_playing_notification_background, null))
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        startForeground(NOTIFY_ID, notification);
        return true;
    }
}
