package com.de.mucify.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
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
import com.de.mucify.ui.ActivityPlayer;
import com.de.mucify.ui.ActivityPlaylistPlayer;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {
    private static final int NOTIFY_ID = 1337;
    private static final float DUCK_VOLUME = .2f;
    private static final String EMPTY_MEDIA_ID = "com.de.mucify.EMPTY_MEDIA";
    private static final String CHANNEL_ID = "com.de.mucify.MediaPlaybackChannel";

    private Playback mPlayback;
    private final Object mPlaybackLock = new Object();

    /**
     * Used to handle the case where the player is paused and we gain audio focus in which case
     * the player would start again. This is set to true if the player is paused.
     */
    private boolean mKeepPausedAfterAudioFocusGain = false;

    /**
     * One global instance of the MediaLibrary. This is placed here to avoid it being unloaded when
     * the phone is shut down and the MediaPlaybackService is running in the background but requires
     * access to the MediaLibrary. (MY_TODO: Terrible code)
     */
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
                    if(!mKeepPausedAfterAudioFocusGain)
                        mMediaSession.getController().getTransportControls().play();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mMediaSession.getController().getTransportControls().pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mPlayback.setVolume(DUCK_VOLUME, DUCK_VOLUME);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // MY_TODO: Release media player here
                    mMediaSession.getController().getTransportControls().pause();
                    break;
            }
        }
    };

    /**
     * Handles the case where headphones are unplugged. The audio shouldn't continue playing because
     * it could disturb people around the user.
     */
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

        // MY_TODO: Find a good location to load MediaLibrary and UserData
        UserData.load(this);
        if(Media == null) {
            Media = new MediaLibrary(this);
            Media.loadAvailableSongs();
            Media.loadAvailableLoops();
            Media.loadAvailablePlaylists();
            setLocalizedUnknownArtist();
        }

        createMediaSession();

        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        createNotificationActions();
        startPlaybackUpdateThread();

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
        for(Playlist p : Media.AvailablePlaylists) {
            MediaDescriptionCompat mediaDesc = new MediaDescriptionCompat.Builder()
                    .setMediaId(p.getMediaId())
                    .setTitle(p.getName())
                    .build();

            mediaItems.add(new MediaBrowserCompat.MediaItem(mediaDesc, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }

        result.sendResult(mediaItems);
    }

    /**
     * Rebuilds the notification for the MediaSession with the new metadata/state and returns it. Should be called whenever a metadata/state change
     * like pause/play, skipToNext/-Previous, ... happens
     */
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
                .setContentIntent(createContentIntent())

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

    /**
     * Creates the required notification channel.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Mucify foreground service notification", NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
        Log.d("Mucify", "Created notification channel");
    }

    /**
     * Rebuilds the metadata for the current Playback.
     */
    private MediaMetadataCompat getMetadata() {
        synchronized (mPlaybackLock) {
            return mMetadataBuilder
                    .putString(MetadataKey.Title, mPlayback.getTitle())
                    .putString(MetadataKey.Artist, mPlayback.getSubtitle())
                    .putLong(MetadataKey.Duration, mPlayback.getDuration())
                    .putLong(MetadataKey.StartPos, mPlayback.getCurrentSong().getStartTime())
                    .putLong(MetadataKey.EndPos, mPlayback.getCurrentSong().getEndTime())
                    .build();
        }
    }

    /**
     * Rebuilds the state for the current Playback.
     */
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

    /**
     * Reposts the notification without requiring a foreground service to be started
     */
    private void repostNotification() {
        mNotificationManager.notify(NOTIFY_ID, buildNotification());
        Log.d("Mucify", "Reposting notification");
    }


    public class MediaSessionCallback extends MediaSessionCompat.Callback {

        /**
         * Called whenever we want to play/unpause the current song. Requests audio focus, starts
         * required services, starts the playback and sets UserData.LastPlayedPlayback to the new
         * Playback that was started/unpaused.
         */
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
                    mKeepPausedAfterAudioFocusGain = false;

                    if(mPlayback instanceof Playlist) {
                        // Called when the song finished and we need to skip to the next one in the playlist
                        mPlayback.getCurrentSong().setOnMediaPlayerCompletionListener(mp -> onSkipToNext());
                    } else {
                        // Called when one loop is done. The MediaPlayer position changes back to startPos
                        // and we need to update seekbars.
                        mPlayback.getCurrentSong().setOnMediaPlayerCompletionListener(mp -> repostNotification());
                    }

                    savePlaybackToSettings();
                }


                // Register BECOME_NOISY BroadcastReceiver
                registerReceiver(myNoisyAudioStreamReceiver, mBecomeNoisyIntentFilter);

                // Put the service in the foreground, post notification
                startForeground(NOTIFY_ID, buildNotification());
                Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPlay");
            }
        }

        /**
         * Pauses the current Playback, reposts the notification without foreground service
         * (MY_TODO: Do we need to repost notification without startForeground or can we just use it?)
         * and sets UserData.LastPlayedPlaybackPos to the current Playback position.
         */
        @Override
        public void onPause() {
            synchronized (mPlaybackLock) {
                mPlayback.pause();
            }
            mKeepPausedAfterAudioFocusGain = true;
//            unregisterReceiver(myNoisyAudioStreamReceiver);

            repostNotification();
//            stopForeground(false);

            savePlaybackToSettings();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPause");
        }

        /**
         * Abandons audio focus, completely stops the service and resets the Playback.
         * (MY_TODO: Doesn't get called when notification is swiped. Should it?)
         */
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

        /**
         * Resets the old Playback if there was one and starts the new one.
         */
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            synchronized (mPlaybackLock) {
                if(mPlayback != null)
                    mPlayback.reset();

                mPlayback = MediaLibrary.getPlaybackFromMediaId(mediaId);
                mPlayback.create(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onPlayFromMediaId " + mediaId);
        }

        /**
         * Seeks Playback to pos.
         * @param pos position to seek to in milliseconds.
         */
        @Override
        public void onSeekTo(long pos) {
            synchronized (mPlaybackLock) {
                mPlayback.seekTo((int)pos);
            }
            repostNotification();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSeekTo " + pos);
        }

        /**
         * Gets the next song in the list and calls onPlay().
         */
        @Override
        public void onSkipToNext() {
            synchronized (mPlaybackLock) {
                mPlayback.reset();
                mPlayback = mPlayback.next(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSkipToNext");
        }

        /**
         * Gets the previous song in the list and calls onPlay().
         */
        @Override
        public void onSkipToPrevious() {
            synchronized (mPlaybackLock) {
                mPlayback.reset();
                mPlayback = mPlayback.previous(MediaPlaybackService.this);
            }
            onPlay();
            Log.d("Mucify", "MediaPlaybackService.MediaSessionCallback.onSkipToPrevious");
        }

        /**
         * Handles custom events to the player. Because we don't want the Playable object to be used
         * in the UI code, we'll have to handle things like setting start/end time like this. All
         * possible events are defined in MediaAction.
         */
        @Override
        public void onCustomAction(String action, Bundle extras) {
            switch(action) {
                case MediaAction.SetStartTime:
                    if(mPlayback instanceof Song && !((Song)mPlayback).isLoop())
                        mPlayback.getCurrentSong().setStartTime(extras.getInt(MediaAction.StartTime));
                    break;
                case MediaAction.SetEndTime:
                    if(mPlayback instanceof Song && !((Song)mPlayback).isLoop())
                        mPlayback.getCurrentSong().setEndTime(extras.getInt(MediaAction.EndTime));
                    break;
            }
        }
    }

    /**
     * Creates and initializes media default media session with Play/Pause actions, MediaSessionCallback and
     * sets the current session token
     */
    private void createMediaSession() {
        mMediaSession = new MediaSessionCompat(this, "com.de.mucify.MediaPlaybackService");
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(stateBuilder.build());
        mMediaSession.setCallback(new MediaSessionCallback());
        setSessionToken(mMediaSession.getSessionToken());
    }

    private void createNotificationActions() {
        mPlayAction = new NotificationCompat.Action(
                R.drawable.play,
                getString(R.string.play),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PLAY));
        mPauseAction = new NotificationCompat.Action(
                R.drawable.pause,
                getString(R.string.pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_PAUSE));
        mNextAction = new NotificationCompat.Action(
                R.drawable.next,
                getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPreviousAction = new NotificationCompat.Action(
                R.drawable.previous,
                getString(R.string.previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
    }

    /**
     * Thread checks every User.AudioUpdateInterval if the song has finished playing. If so
     * the song will either be restarted (Song) or the next song will be played (Playlist)
     */
    private void startPlaybackUpdateThread() {

        new Thread(() -> {
            Thread.setDefaultUncaughtExceptionHandler(Util.UncaughtExceptionLogger);

            while(true) {

                synchronized (mPlaybackLock) {
                    if(mPlayback != null && mPlayback.isCreated()) {
                        int currentPos = mPlayback.getCurrentPosition();
                        Song currentSong = mPlayback.getCurrentSong();

                        if(currentPos >= currentSong.getEndTime() || currentPos < currentSong.getStartTime()) {
                            if(mPlayback instanceof Playlist) {
                                mMediaSession.getController().getTransportControls().skipToNext();
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
    }

    /**
     * Creates the PendingIntent to open the (playlist)player activity.
     */
    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(this, mPlayback instanceof Playlist ? ActivityPlaylistPlayer.class : ActivityPlayer.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        openUI.putExtra("MediaId", mPlayback.getMediaId());
        openUI.putExtra("IsPlaying", true);
            return PendingIntent.getActivity(
                    this, 0, openUI, PendingIntent.FLAG_CANCEL_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
    }

    private void savePlaybackToSettings() {
        synchronized (UserData.SettingsLock) {
                UserData.LastPlayedPlayback = mPlayback.getPath();
                UserData.LastPlayedPlaybackPos = mPlayback.getCurrentPosition();
                if(mPlayback instanceof Playlist) {
                    Playlist playlist = (Playlist)mPlayback;
                    UserData.LastPlayedPlaybackInPlaylist = playlist.getCurrentSong().getPath();
                }
        }
        UserData.save();
    }

    /**
     * We need to translate "Unknown Artist". Thus we'll set it with string resource
     */
    private void setLocalizedUnknownArtist() {
        for(Song s : Media.AvailableSongs)
            if(s.isArtistUnknown())
                s.setArtist(getString(R.string.unknown_artist));

        for(Song s : Media.AvailableLoops)
            if(s.isArtistUnknown())
                s.setArtist(getString(R.string.unknown_artist));

        for(Playlist p : Media.AvailablePlaylists)
            for(Song s : p.getSongs())
                if(s.isArtistUnknown())
                    s.setArtist(getString(R.string.unknown_artist));
    }
}
