package com.de.mucify.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.receiver.ForegroundNotificationClickReceiver;

public class MediaSessionService extends Service {
    static MediaSessionService sInstance = null;

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private MediaNotificationManager mMediaNotificationManager;


    private static final int NOTIFY_ID = 1337;
    private boolean mAlreadyReset = false;

    public MediaSessionService() {
//        super("com.de.mucify.MediaSessionService");

        if(sInstance != null)
            sInstance.reset();

        sInstance = this;
    }

    public void reset() {
        AudioController.get().reset();
    }

    @Override
    public void onCreate() {
        if(AudioController.get().isSongNull() || mAlreadyReset)
            return;

        if(mMediaNotificationManager == null)
            mMediaNotificationManager = new MediaNotificationManager(this);

        if(!startCustomForegroundService())
            return;

        registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);

        AudioController.get().addOnSongResetListener(song -> {
            stopForeground(true);
            mAlreadyReset = true;
        }, 0);
        AudioController.get().addOnSongPausedListener(song -> startCustomForegroundService(), 0);
        AudioController.get().addOnSongUnpausedListener(song -> startCustomForegroundService(), 0);
        AudioController.get().addOnNextSongListener(nextSong -> startCustomForegroundService(), 0);
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent)intent.getExtras().get("android.intent.extra.KEY_EVENT");
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                AudioController.get().pauseSong();
            } else {
                AudioController.get().unpauseSong();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static MediaSessionService get() { return sInstance; }

    private boolean startCustomForegroundService() {
        Notification notification = mMediaNotificationManager.buildNotification(AudioController.get().isSongPlaying());
        startForeground(NOTIFY_ID, notification);
        return true;
    }
}
