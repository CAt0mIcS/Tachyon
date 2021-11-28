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
import android.media.session.PlaybackState;
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

    private NotificationCompat.Action mPlayAction;
    private NotificationCompat.Action mPauseAction;

    public MediaNotificationManager(MediaSessionService service) {
        mService = service;

        mNotificationManager = mService.getSystemService(NotificationManager.class);
        mMediaSessionCompat = new MediaSessionCompat(mService, "MucifySongService");
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                AudioController.get().unpauseSong();
            }

            @Override
            public void onPause() {
                AudioController.get().pauseSong();
            }

            @Override
            public void onStop() {
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                AudioController.get().seekSongTo(pos);
            }
        });
        createNotificationChannel();

        mPlayAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_arrow_black,
                        "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_PLAY));
        mPauseAction =
                new NotificationCompat.Action(
                        R.drawable.ic_pause_black,
                        "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_PAUSE));

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();

        AudioController.get().addOnSongSeekedListener(song -> mMediaSessionCompat.setPlaybackState(getState()), 0);
    }

    public Notification buildNotification(boolean isPlaying) {
        mMediaSessionCompat.setMetadata(getMetadata());
        mMediaSessionCompat.setPlaybackState(getState());

        IntentFilter filter = new IntentFilter(ACTION_PREVIOUS);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_NEXT);
        mService.registerReceiver(mNotificationReceiver, filter);

        return new NotificationCompat.Builder(mService, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_black)
                .setOnlyAlertOnce(true)  // show notification only first time
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        mService,
                                        PlaybackStateCompat.ACTION_STOP))
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))

                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent())

                // Add pause or play button
                .addAction(isPlaying ? mPauseAction : mPlayAction)

                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_PAUSE))

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
        long actions = (AudioController.get().isSongPlaying() ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY) |
                PlaybackStateCompat.ACTION_SEEK_TO;
//                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
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

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, SingleAudioPlayActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("PreserveSong", true);
        return PendingIntent.getActivity(
                mService, 0, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
