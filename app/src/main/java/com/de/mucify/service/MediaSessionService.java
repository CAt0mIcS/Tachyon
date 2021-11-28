package com.de.mucify.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.IBinder;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import com.de.mucify.playable.AudioController;
import com.de.mucify.receiver.BecomingNoisyReceiver;

public class MediaSessionService extends Service {
    static MediaSessionService sInstance = null;

    private final IntentFilter mNoisyAudioIntent = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final BecomingNoisyReceiver mNoisyAudioReceiver = new BecomingNoisyReceiver();

    private MediaNotificationManager mMediaNotificationManager;

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChanged = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    AudioController.get().pauseSong();
                    mHasAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if(AudioController.get().isPaused()) AudioController.get().unpauseSong();
                    else AudioController.get().startSong();
                    mHasAudioFocus = true;
                    break;
            }
        }
    };
    private final AudioFocusRequest mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setOnAudioFocusChangeListener(mOnAudioFocusChanged)
            .setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
            .build();
    private boolean mHasAudioFocus = false;

    public static final int NOTIFY_ID = 1337;


    public MediaSessionService() {
        if(sInstance != null)
            sInstance.reset();

        sInstance = this;
    }

    public void reset() {
        AudioController.get().reset();
        stopSelf();
    }

    @Override
    public void onCreate() {
        if(AudioController.get().isSongNull())
            return;

        mMediaNotificationManager = new MediaNotificationManager(this);
        if(!requestAudioFocus(this))
            return;
        mHasAudioFocus = true;

        startCustomForegroundService();
        registerReceiver(mNoisyAudioReceiver, mNoisyAudioIntent);

        AudioController.get().addOnSongResetListener(song -> {
            stopForeground(true);
            stopSelf();
        }, 0);
        AudioController.get().addOnSongPausedListener(song -> startCustomForegroundService(), 0);
        AudioController.get().addOnSongUnpausedListener(song -> startCustomForegroundService(), 0);
        AudioController.get().addOnNextSongListener(nextSong -> startCustomForegroundService(), 0);
    }

    @Override
    public void onDestroy() {
        try {
            mMediaNotificationManager.release();
            unregisterReceiver(mNoisyAudioReceiver);
            abandonAudioFocus(this);
        } catch(IllegalArgumentException ignored) {}
        sInstance = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction())) {
            KeyEvent keyEvent = (KeyEvent)intent.getExtras().get("android.intent.extra.KEY_EVENT");
            switch(keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PAUSE: AudioController.get().pauseSong(); break;
                case KeyEvent.KEYCODE_MEDIA_PLAY: if(requestAudioFocus(this)) AudioController.get().unpauseSong(); break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    AudioController.get().next(this);
                    if(!mHasAudioFocus)
                        AudioController.get().pauseSong();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    AudioController.get().previous(this);
                    if(!mHasAudioFocus)
                        AudioController.get().pauseSong();
                    break;
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

    public boolean hasAudioFocus() { return mHasAudioFocus; }

    private void startCustomForegroundService() {
        Notification notification = mMediaNotificationManager.buildNotification(AudioController.get().isSongPlaying());
        startForeground(NOTIFY_ID, notification);
    }

    private boolean requestAudioFocus(Context context) {
        if(((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(mAudioFocusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mHasAudioFocus = true;
            return true;
        }
        return false;
    }

    private int abandonAudioFocus(Context context) {
        mHasAudioFocus = false;
        return ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocusRequest(mAudioFocusRequest);
    }
}
