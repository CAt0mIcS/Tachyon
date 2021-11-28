package com.de.mucify.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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

public class MediaSessionService extends IntentService {
    static MediaSessionService sInstance = null;

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private MediaNotificationManager mMediaNotificationManager;


    private static final int NOTIFY_ID = 1337;
    private boolean mAlreadyReset = false;

    private final Object mMutex = new Object();

    public MediaSessionService() {
        super("com.de.mucify.MediaSessionService");

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

        if(mMediaNotificationManager == null)
            mMediaNotificationManager = new MediaNotificationManager(this);

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
            mMediaNotificationManager.release();
            unregisterReceiver(mNoisyAudioReceiver);
        } catch(IllegalArgumentException ignored) {}
        sInstance = null;
    }

    public static MediaSessionService get() { return sInstance; }

    private boolean startCustomForegroundService(@DrawableRes int playPauseIconID) {
        Notification notification = mMediaNotificationManager.buildNotification(playPauseIconID);
        startForeground(NOTIFY_ID, notification);
        return true;
    }
}
