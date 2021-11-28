package com.de.mucify.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.receiver.ForegroundNotificationClickReceiver;

public class SongPlayForegroundService extends IntentService {
    static SongPlayForegroundService sInstance = null;

    private final ForegroundNotificationClickReceiver mNotificationReceiver = new ForegroundNotificationClickReceiver();

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private NotificationManager mNotificationManager;

    private static final String CHANNEL_ID = "com.de.mucify.player";
    public static final String ACTION_PREVIOUS = "com.de.mucify.ACTION_PREVIOUS";
    public static final String ACTION_PLAY_PAUSE = "com.de.mucify.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.de.mucify.ACTION_NEXT";


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

        if(mNotificationManager == null)
            mNotificationManager = getSystemService(NotificationManager.class);
        assert mNotificationManager != null;

        if(!startCustomForegroundService(R.drawable.ic_pause_black))
            return;

        registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);

        AudioController.get().addOnSongResetListener(song -> {
            stopForeground(true);
            mAlreadyReset = true;
            synchronized (mMutex) {
                mMutex.notify();
            }
        }, 0);
        AudioController.get().addOnSongPausedListener(song -> startCustomForegroundService(R.drawable.ic_play_arrow_black), 0);
        AudioController.get().addOnSongUnpausedListener(song -> startCustomForegroundService(R.drawable.ic_pause_black), 0);
        AudioController.get().addOnNextSongListener(nextSong -> startCustomForegroundService(AudioController.get().isPaused() ? R.drawable.ic_play_arrow_black : R.drawable.ic_pause_black), 0);

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
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Song playing foreground service notification", NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(this, "Mucify");
        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, AudioController.get().getSongTitle())
            .putString(MediaMetadata.METADATA_KEY_ARTIST, AudioController.get().getSongArtist())
            .build());

        IntentFilter filter = new IntentFilter(ACTION_PREVIOUS);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_NEXT);
        registerReceiver(mNotificationReceiver, filter);

        Intent intentPrevious = new Intent(ACTION_PREVIOUS);
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(this, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayPause = new Intent(ACTION_PLAY_PAUSE);
        intentPlayPause.putExtra("PlayPause", playPauseIconID);
        PendingIntent pendingIntentPlayPause = PendingIntent.getBroadcast(this, 0, intentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clickIntent = new Intent(this, SingleAudioPlayActivity.class);
        clickIntent.putExtra("PreserveSong", true);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(this, 0, clickIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_black)
                .setOnlyAlertOnce(true)  // show notification only first time
                .setShowWhen(false)
                .setContentIntent(pendingClickIntent)
                .addAction(R.drawable.ic_previous_black, "Previous", pendingIntentPrevious)
                .addAction(playPauseIconID, "Play/Pause", pendingIntentPlayPause)
                .addAction(R.drawable.ic_next_black, "Next", pendingIntentNext)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        notificationManagerCompat.notify(NOTIFY_ID, notification);
        startForeground(NOTIFY_ID, notification);
        return true;
    }
}
