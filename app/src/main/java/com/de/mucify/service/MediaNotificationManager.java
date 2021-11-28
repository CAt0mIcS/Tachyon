package com.de.mucify.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.de.mucify.R;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.receiver.BecomingNoisyReceiver;
import com.de.mucify.receiver.ForegroundNotificationClickReceiver;

public class MediaNotificationManager {
    private MediaSessionService mService;

    private static final String TAG = MediaNotificationManager.class.getSimpleName();

    private final ForegroundNotificationClickReceiver mNotificationReceiver = new ForegroundNotificationClickReceiver();

    private static final String CHANNEL_ID = "com.de.mucify.player";
    public static final String ACTION_PREVIOUS = "com.de.mucify.ACTION_PREVIOUS";
    public static final String ACTION_PLAY_PAUSE = "com.de.mucify.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.de.mucify.ACTION_NEXT";

    private MediaSessionCompat mMediaSessionCompat;
    private NotificationManager mNotificationManager;


    public MediaNotificationManager(MediaSessionService service) {
        mService = service;

        mNotificationManager = mService.getSystemService(NotificationManager.class);
        mMediaSessionCompat = new MediaSessionCompat(mService, "MucifySongService");
        createNotificationChannel();
    }

    public Notification buildNotification(@DrawableRes int playPauseIconID) {
        mMediaSessionCompat.setMetadata(getMetadata());

        IntentFilter filter = new IntentFilter(ACTION_PREVIOUS);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_NEXT);
        mService.registerReceiver(mNotificationReceiver, filter);

        Intent intentPrevious = new Intent(ACTION_PREVIOUS);
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(mService, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlayPause = new Intent(ACTION_PLAY_PAUSE);
        intentPlayPause.putExtra("PlayPause", playPauseIconID);
        PendingIntent pendingIntentPlayPause = PendingIntent.getBroadcast(mService, 0, intentPlayPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(ACTION_NEXT);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(mService, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent clickIntent = new Intent(mService, SingleAudioPlayActivity.class);
        clickIntent.putExtra("PreserveSong", true);
        PendingIntent pendingClickIntent = PendingIntent.getActivity(mService, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(mService, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_black)
                .setOnlyAlertOnce(true)  // show notification only first time
                .setShowWhen(false)
                .setContentIntent(pendingClickIntent)
                .addAction(R.drawable.ic_previous_black, "Previous", pendingIntentPrevious)
                .addAction(playPauseIconID, "Play/Pause", pendingIntentPlayPause)
                .addAction(R.drawable.ic_next_black, "Next", pendingIntentNext)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    public void release() {
        mService.unregisterReceiver(mNotificationReceiver);
        mMediaSessionCompat.release();
    }

    private MediaMetadataCompat getMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, AudioController.get().getSongTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, AudioController.get().getSongArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, AudioController.get().getSongDuration())
                .build();
    }

    private PlaybackStateCompat getState() {
        long actions = AudioController.get().isSongPlaying() ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY;
        int state = AudioController.get().isSongPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        return new PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state,
                AudioController.get().getCurrentSongPosition(),
                1.0f,
                SystemClock.elapsedRealtime())
            .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Song playing foreground service notification", NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
    }
}
