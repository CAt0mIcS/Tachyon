package com.de.mucify.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.de.mucify.R;
import com.de.mucify.activity.MultiAudioPlayActivity;
import com.de.mucify.activity.SingleAudioActivity;
import com.de.mucify.activity.SingleAudioPlayActivity;
import com.de.mucify.playable.AudioController;
import com.de.mucify.playable.Song;
import com.de.mucify.util.MediaLibrary;

import java.util.ArrayList;
import java.util.List;

public class MediaNotificationManager {
    private final MediaSessionService mService;

    private static final String CHANNEL_ID = "com.de.mucify.player";

    private final MediaSessionCompat mMediaSessionCompat;
    private final NotificationManager mNotificationManager;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPreviousAction;

    public MediaNotificationManager(MediaSessionService service) {
        mService = service;

        mNotificationManager = mService.getSystemService(NotificationManager.class);

        mMediaSessionCompat = new MediaSessionCompat(mService, "MucifySongService");
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                mMediaSessionCompat.setActive(true);
                AudioController.get().unpauseSong();
            }

            @Override
            public void onPause() {
                AudioController.get().pauseSong();
            }

            @Override
            public void onStop() {
                mMediaSessionCompat.setActive(false);
                mService.reset();
            }

            @Override
            public void onSeekTo(long pos) {
                AudioController.get().seekSongTo(pos);
            }

            @Override
            public void onSkipToNext() {
                AudioController.get().next(mService);
                if(!mService.hasAudioFocus())
                    AudioController.get().pauseSong();
            }

            @Override
            public void onSkipToPrevious() {
                AudioController.get().previous(mService);
                if(!mService.hasAudioFocus())
                    AudioController.get().pauseSong();
            }
        });
        createNotificationChannel();

        mPlayAction = new NotificationCompat.Action(
                R.drawable.ic_play_arrow_black,
                "Play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_PLAY));
        mPauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause_black,
                "Pause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_PAUSE));
        mNextAction = new NotificationCompat.Action(
                R.drawable.ic_next_black,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPreviousAction = new NotificationCompat.Action(
                R.drawable.ic_previous_black,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        // Cancel all notifications to handle the case where the Service was killed and
        // restarted by the system.
        mNotificationManager.cancelAll();

        AudioController.get().addOnSongSeekedListener(song -> mMediaSessionCompat.setPlaybackState(getState()), 0);
    }

    public Notification buildNotification(boolean isPlaying) {
        mMediaSessionCompat.setMetadata(getMetadata());
        mMediaSessionCompat.setPlaybackState(getState());

//        List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
//        int i = 0;
//        for(Song s : MediaLibrary.AvailableSongs) {
//            MediaDescriptionCompat desc = new MediaDescriptionCompat.Builder()
//                    .setTitle(s.getTitle())
//                    .build();
//
//            queue.add(new MediaSessionCompat.QueueItem(desc, i));
//            ++i;
//        }
//
//        mMediaSessionCompat.setQueue(queue);
//        mMediaSessionCompat.setQueueTitle("TestQueue");

        return new NotificationCompat.Builder(mService, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note_black)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        mService,
                                        PlaybackStateCompat.ACTION_STOP))
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))

                // Pending intent that is fired when user clicks on notification.
                .setContentIntent(createContentIntent())

                // Add previous song action
                .addAction(mPreviousAction)
                // Add pause or play button
                .addAction(isPlaying ? mPauseAction : mPlayAction)
                // Add next song action
                .addAction(mNextAction)

                // Add the metadata for the currently playing track
                .setContentTitle(AudioController.get().getSongTitle())
                .setContentText(AudioController.get().getSongArtist())
                .setLargeIcon(BitmapFactory.decodeResource(mService.getResources(), R.drawable.ic_launcher_foreground))

                // Make the transport controls visible on the lockscreen
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // When notification is deleted (when playback is paused and notification can be
                // deleted) fire MediaButtonPendingIntent with ACTION_PAUSE.
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_PAUSE))

                .build();
    }

    public void release() {
        mMediaSessionCompat.release();
    }

    private MediaMetadataCompat getMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, AudioController.get().getSongTitle())
                .putString(MediaMetadata.METADATA_KEY_ARTIST, AudioController.get().getSongArtist())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, AudioController.get().getSongDuration())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(mService.getResources(), R.drawable.ic_launcher_foreground))
                .build();
    }

    private PlaybackStateCompat getState() {
        long actions = (AudioController.get().isSongPlaying() ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY) |
                PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
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
        Intent openUI = new Intent(mService, AudioController.get().isPlaylistNull() ? SingleAudioPlayActivity.class : MultiAudioPlayActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("PreserveAudio", true);
        return PendingIntent.getActivity(
                mService, 0, openUI, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
