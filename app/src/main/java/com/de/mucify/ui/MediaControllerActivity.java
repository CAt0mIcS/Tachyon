package com.de.mucify.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.de.mucify.service.MediaAction;
import com.google.android.gms.cast.framework.CastContext;

import java.util.ArrayList;

/**
 * Class which dispatches media events to the active controller (either MediaBrowserController or CastController)
 */
public class MediaControllerActivity extends AppCompatActivity implements IMediaController {
    private MediaBrowserController mBrowserController;
    private static CastController mCastController;

    private final ArrayList<Callback> mCallbacks = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lazily initialize cast context
        CastContext.getSharedInstance(this);

        mBrowserController = new MediaBrowserController(this, mCallbacks);
        if (mCastController == null)
            mCastController = new CastController();
        mCastController.setActivity(this);

        // Removes our notification because Google Cast has its own
        addCallback(new Callback() {
            @Override
            public void onCastConnected() {
                mBrowserController.sendCustomAction(MediaAction.CastStarted);
            }
        });
    }

    public void initializeToolbar() {
        mCastController.initializeToolbar(this);
    }

    public ArrayList<Callback> getCallbacks() {
        return mCallbacks;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBrowserController.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mCastController.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCastController.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBrowserController.onStop();
    }


    @Override
    public void unpause() {
        if (mCastController.isCasting())
            mCastController.unpause();
        else
            mBrowserController.unpause();
    }

    @Override
    public void pause() {
        if (mCastController.isCasting())
            mCastController.pause();
        else
            mBrowserController.pause();
    }

    @Override
    public void seekTo(int millis) {
        if (mCastController.isCasting())
            mCastController.seekTo(millis);
        else
            mBrowserController.seekTo(millis);
    }

    @Override
    public void play(String mediaId) {
        mCastController.setPlayback(mediaId);
        if (mCastController.isCasting())
            mCastController.play(mediaId);
        else
            mBrowserController.play(mediaId);
    }

    @Override
    public boolean isPlaying() {
        if (mCastController.isCasting())
            return mCastController.isPlaying();
        return mBrowserController.isPlaying();
    }

    @Override
    public boolean isCreated() {
        if (mCastController.isCasting())
            return mCastController.isCreated();
        return mBrowserController.isCreated();
    }

    @Override
    public boolean isPaused() {
        if (mCastController.isCasting())
            return mCastController.isPaused();
        return mBrowserController.isPaused();
    }

    @Override
    public void setStartTime(int millis) {
        if (mCastController.isCasting())
            mCastController.setStartTime(millis);
        else
            mBrowserController.setStartTime(millis);
    }

    @Override
    public void setEndTime(int millis) {
        if (mCastController.isCasting())
            mCastController.setEndTime(millis);
        else
            mBrowserController.setEndTime(millis);
    }

    @Override
    public void saveAsLoop(String loopName) {
        if (mCastController.isCasting())
            mCastController.saveAsLoop(loopName);
        else
            mBrowserController.saveAsLoop(loopName);
    }

    @Override
    public int getStartTime() {
        if (mCastController.isCasting())
            return mCastController.getStartTime();
        return mBrowserController.getStartTime();
    }

    @Override
    public int getEndTime() {
        if (mCastController.isCasting())
            return mCastController.getEndTime();
        return mBrowserController.getEndTime();
    }

    @Override
    public int getCurrentPosition() {
        if (mCastController.isCasting())
            return mCastController.getCurrentPosition();
        return mBrowserController.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        if (mCastController.isCasting())
            return mCastController.getDuration();
        return mBrowserController.getDuration();
    }

    @Override
    public String getSongTitle() {
        if (mCastController.isCasting())
            return mCastController.getSongTitle();
        return mBrowserController.getSongTitle();
    }

    @Override
    public String getSongArtist() {
        if (mCastController.isCasting())
            return mCastController.getSongArtist();
        return mBrowserController.getSongArtist();
    }


    public void addCallback(Callback c) {
        mCallbacks.add(c);
    }

    public void removeCallback(Callback c) {
        mCallbacks.remove(c);
    }

    public void onConnected() {
    }

    public void onDisconnected() {
    }

    public abstract static class Callback {
        public void onStart() {
        }

        public void onPause() {
        }

        public void onTitleChanged(String title) {
        }

        public void onArtistChanged(String artist) {
        }

        public void onSeekTo(int millis) {
        }

        public void onMediaIdChanged(String mediaId) {
        }

        public void onCastConnected() {
        }

        public void onCastDisconnected() {
        }
    }
}
