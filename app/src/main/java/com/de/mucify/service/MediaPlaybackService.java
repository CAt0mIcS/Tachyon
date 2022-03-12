package com.de.mucify.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.de.mucify.PermissionManager;
import com.de.mucify.R;
import com.de.mucify.Util;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String MEDIA_ROOT_ID = "com.de.mucify.ROOT_MEDIA";
    private static final String EMPTY_MEDIA_ID = "com.de.mucify.EMPTY_MEDIA";
    private static final String CHANNEL_ID = "com.de.mucify.MediaPlaybackChannel";

    private IntentFilter mBecomeNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangedListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };
    private final BroadcastReceiver myNoisyAudioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mMediaPlayer.pause();
            }
        }
    };

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaPlayer mMediaPlayer;

    private NotificationCompat.Action mPlayAction;
    private NotificationCompat.Action mPauseAction;
    private NotificationCompat.Action mNextAction;
    private NotificationCompat.Action mPreviousAction;

    @Override
    public void onCreate() {
        super.onCreate();

        mMediaSession = new MediaSessionCompat(this, "com.de.mucify.MediaPlaybackService");
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        setSessionToken(mMediaSession.getSessionToken());

        mMediaPlayer = MediaPlayer.create(this, Uri.parse("/storage/emulated/0/Music/Bone Dry - Tristam.mp3"));
        mMediaPlayer.setLooping(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        mPlayAction = new NotificationCompat.Action(
                R.drawable.pause,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY));
        mPauseAction = new NotificationCompat.Action(
                R.drawable.pause,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PAUSE));
        mNextAction = new NotificationCompat.Action(
                R.drawable.pause,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPreviousAction = new NotificationCompat.Action(
                R.drawable.pause,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //  Browsing not allowed
        if (TextUtils.equals(EMPTY_MEDIA_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }


    public Notification buildNotification(boolean isPlaying) {
        mMediaSession.setMetadata(getMetadata());
        mMediaSession.setPlaybackState(getState());

        return new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.ic_music_note_black)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_STOP))
                        .setMediaSession(mMediaSession.getSessionToken()))

                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(mMediaSession.getController().getSessionActivity())

                // Add previous song action
                .addAction(mPreviousAction)
                // Add pause or play button
                .addAction(isPlaying ? mPauseAction : mPlayAction)
                // Add next song action
                .addAction(mNextAction)

                // Add the metadata for the currently playing track
                .setContentTitle("Cosmic Storm")
                .setContentText("A Himitsu")

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_PAUSE))

                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Mucify foreground service notification", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private MediaMetadataCompat getMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, "Cosmic Storm")
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "A Himitsu")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 1000)
                .build();
    }

    private PlaybackStateCompat getState() {
        long actions = (mMediaPlayer.isPlaying() ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY) |
                PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        int state = mMediaPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

        return new PlaybackStateCompat.Builder()
                .setActions(actions)
                .setState(state,
                        mMediaPlayer.getCurrentPosition(),
                        1.0f,
                        SystemClock.elapsedRealtime())
                .build();
    }


    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            if (Util.requestAudioFocus(MediaPlaybackService.this, mAudioFocusChangedListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Start the service
                startService(new Intent(MediaPlaybackService.this, MediaBrowserService.class));

                // Set the session active  (and update metadata and state)
                mMediaSession.setActive(true);

                // start the player (custom call)
                mMediaPlayer.start();

                // Register BECOME_NOISY BroadcastReceiver
                registerReceiver(myNoisyAudioStreamReceiver, mBecomeNoisyIntentFilter);

                // Put the service in the foreground, post notification
                startForeground(1337, buildNotification(true));
            }
        }

        @Override
        public void onPause() {
            mMediaPlayer.pause();
            unregisterReceiver(myNoisyAudioStreamReceiver);

            mMediaSession.setMetadata(getMetadata());
            mMediaSession.setPlaybackState(getState());

            stopForeground(false);
        }

        @Override
        public void onStop() {
            Util.abandonAudioFocus(MediaPlaybackService.this, mAudioFocusChangedListener);
            unregisterReceiver(myNoisyAudioStreamReceiver);
            stopSelf();
            mMediaSession.setActive(false);
            mMediaPlayer.stop();
            stopForeground(false);
        }

        @Override
        public void onSeekTo(long pos) {
        }

        @Override
        public void onSkipToNext() {
        }

        @Override
        public void onSkipToPrevious() {
        }
    }
}
