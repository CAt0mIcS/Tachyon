package com.de.mucify.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.de.mucify.MediaLibrary;
import com.de.mucify.R;
import com.de.mucify.UserData;
import com.de.mucify.Util;
import com.de.mucify.player.Playback;
import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final String EMPTY_MEDIA_ID = "com.de.mucify.EMPTY_MEDIA";
    private static final String CHANNEL_ID = "com.de.mucify.MediaPlaybackChannel";

    private Playback mPlayback;
    private final Object mPlaybackLock = new Object();
    public static MediaLibrary Media;

    private final IntentFilter mBecomeNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangedListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if(UserData.IgnoreAudioFocus)
                return;

            Log.d("Mucify", "Audio focus changed " + focusChange);
            switch(focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                    mPlayback.setVolume(1.f, 1.f);
                    mMediaSession.getController().getTransportControls().play();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mPlayback.setVolume(.2f, .2f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // MY_TODO: Release media player here
                    mMediaSession.getController().getTransportControls().pause();
                    break;
            }
        }
    };
    private final BroadcastReceiver myNoisyAudioStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mMediaSession.getController().getTransportControls().pause();
                Log.d("Mucify", "Became noisy");
            }
        }
    };

    private MediaSessionCompat mMediaSession;

    private NotificationManager mNotificationManager;
    private final MediaMetadataCompat.Builder mMetadataBuilder = new MediaMetadataCompat.Builder();
    private final PlaybackStateCompat.Builder mPlaybackStateBuilder = new PlaybackStateCompat.Builder();

    private NotificationCompat.Action mPlayAction;
    private NotificationCompat.Action mPauseAction;
    private NotificationCompat.Action mNextAction;
    private NotificationCompat.Action mPreviousAction;

    @Override
    public void onCreate() {
        super.onCreate();

        UserData.load(this);
        if(Media == null) {
            Media = new MediaLibrary(this);
            Media.loadAvailableSongs();
            Media.loadAvailableLoops();
            Media.loadAvailablePlaylists();
        }

        mMediaSession = new MediaSessionCompat(this, "com.de.mucify.MediaPlaybackService");
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(stateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        setSessionToken(mMediaSession.getSessionToken());

        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        mPlayAction = new NotificationCompat.Action(
                R.drawable.play,
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
                R.drawable.next,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPreviousAction = new NotificationCompat.Action(
                R.drawable.previous,
                "Previous",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        new Thread(() -> {
            Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);

            while(true) {

                synchronized (mPlaybackLock) {
                    if(mPlayback != null && mPlayback.isCreated()) {
                        int currentPos = mPlayback.getCurrentPosition();
                        Song currentSong = mPlayback.getCurrentSong();

                        if(currentPos >= currentSong.getEndTime() || currentPos < currentSong.getStartTime()) {
                            if(mPlayback instanceof Playlist) {
                                mPlayback.next(this);
                            }
                            else {
                                mMediaSession.getController().getTransportControls().seekTo(currentSong.getStartTime());
                            }

                        }
                    }
                }

                try {
                    synchronized (UserData.SettingsLock) {
                        Thread.sleep(UserData.AudioUpdateInterval);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Log.d("Mucify", "Created MediaPlaybackService");

        Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //  Browsing not allowed
        if (TextUtils.equals(EMPTY_MEDIA_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        for(Song s : Media.AvailableSongs) {
            MediaDescriptionCompat mediaDesc = new MediaDescriptionCompat.Builder()
                    .setMediaId(s.getMediaId())
                    .setTitle(s.getTitle())
                    .setSubtitle(s.getSubtitle())
                    .build();

            mediaItems.add(new MediaBrowserCompat.MediaItem(mediaDesc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        for(Song s : Media.AvailableLoops) {
            MediaDescriptionCompat mediaDesc = new MediaDescriptionCompat.Builder()
                    .setMediaId(s.getMediaId())
                    .setTitle(s.getTitle())
                    .setSubtitle(s.getSubtitle())
                    .build();

            mediaItems.add(new MediaBrowserCompat.MediaItem(mediaDesc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }
        // MY_TODO: Playlists

        result.sendResult(mediaItems);
    }


    public Notification buildNotification() {
        Log.d("Mucify", "Rebuilding notification");

        PlaybackStateCompat playbackState = getState();
        MediaMetadataCompat metadata = getMetadata();

        mMediaSession.setMetadata(metadata);
        mMediaSession.setPlaybackState(playbackState);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.pause)
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
                .addAction(playbackState.getState() == PlaybackStateCompat.STATE_PLAYING ? mPauseAction : mPlayAction)
                // Add next song action
                .addAction(mNextAction)

                // Add the metadata for the currently playing track
                .setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))

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
        mNotificationManager.createNotificationChannel(channel);
        Log.d("Mucify", "Created notification channel");
    }

    private MediaMetadataCompat getMetadata() {
        synchronized (mPlaybackLock) {
            return mMetadataBuilder
                    .putString(MediaMetadata.METADATA_KEY_TITLE, mPlayback.getTitle())
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, mPlayback.getSubtitle())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mPlayback.getDuration())
                    .build();
        }
    }

    private PlaybackStateCompat getState() {
        synchronized (mPlaybackLock) {
            long actions = (mPlayback.isPlaying() ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY) |
                    PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            int state = mPlayback.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;

            return mPlaybackStateBuilder
                    .setActions(actions)
                    .setState(state,
                            mPlayback.getCurrentPosition(),
                            1.0f,
                            SystemClock.elapsedRealtime())
                    .build();
        }
    }

    private void repostNotification() {
        mNotificationManager.notify(1337, buildNotification());
        Log.d("Mucify", "Reposting notification");
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
                synchronized (mPlaybackLock) {
                    if(!mPlayback.isCreated())
                        mPlayback.create(MediaPlaybackService.this);

                    mPlayback.start(MediaPlaybackService.this);
                    synchronized (UserData.SettingsLock) {
                        if(mPlayback instanceof Song) {
                            if(((Song)mPlayback).isLoop())
                                UserData.LastPlayedPlayback = ((Song)mPlayback).getLoopPath();
                            else
                                UserData.LastPlayedPlayback = ((Song)mPlayback).getSongPath();
                        }
                        else
                            UserData.LastPlayedPlayback = ((Playlist)mPlayback).getCurrentAudioPath();
                    }
                    UserData.save();
                }


                // Register BECOME_NOISY BroadcastReceiver
                registerReceiver(myNoisyAudioStreamReceiver, mBecomeNoisyIntentFilter);

                // Put the service in the foreground, post notification
                startForeground(1337, buildNotification());
                Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPlay");
            }
        }

        @Override
        public void onPause() {
            synchronized (mPlaybackLock) {
                mPlayback.pause();
            }
//            unregisterReceiver(myNoisyAudioStreamReceiver);

            repostNotification();
//            stopForeground(false);

            synchronized (UserData.SettingsLock) {
                UserData.LastPlayedPlaybackPos = mPlayback.getCurrentPosition();
            }
            UserData.save();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPause");
        }

        @Override
        public void onStop() {
            Util.abandonAudioFocus(MediaPlaybackService.this, mAudioFocusChangedListener);
            unregisterReceiver(myNoisyAudioStreamReceiver);
            stopSelf();
            mMediaSession.setActive(false);
            synchronized (mPlaybackLock) {
                mPlayback.reset();
            }
            mPlayback = null;
            stopForeground(false);
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onStop");
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            synchronized (mPlaybackLock) {
                if(mPlayback != null)
                    mPlayback.reset();

                mPlayback = Util.getPlaybackFromMediaId(mediaId);
                mPlayback.create(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPlayFromMediaId " + mediaId);
        }

        @Override
        public void onSeekTo(long pos) {
            synchronized (mPlaybackLock) {
                mPlayback.seekTo((int)pos);
            }
            repostNotification();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSeekTo " + pos);
        }

        @Override
        public void onSkipToNext() {
            synchronized (mPlaybackLock) {
                mPlayback.reset();
                mPlayback = mPlayback.next(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSkipToNext");
        }

        @Override
        public void onSkipToPrevious() {
            synchronized (mPlaybackLock) {
                mPlayback.reset();
                mPlayback = mPlayback.previous(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSkipToPrevious");
        }
    }
}
