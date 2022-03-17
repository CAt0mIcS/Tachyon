package com.de.mucify.player;


import android.content.Context;
import java.util.ArrayList;

public abstract class Playback {
    protected ArrayList<Callback> mCallbacks = new ArrayList<>();

    public abstract static class Callback {
        public void onStart() {}
        public void onRestart() {}
        public void onPause() {}
        public void onSeek(int millis) {}
        public void onStop() {}
        public void onReset() {}
        public void onNext(Song next) {}
        public void onPrevious(Song previous) {}
    }

    public void addCallback(Callback callback) { mCallbacks.add(callback); }
    public void removeCallback(Callback callback) { mCallbacks.remove(callback); }

    public abstract void start(Context context);
    public abstract void pause();
    public abstract boolean isPlaying();
    public boolean isPaused() { return !isPlaying(); }
    public abstract void seekTo(int millis);
    public abstract int getDuration();
    public abstract int getCurrentPosition();
    public abstract void stop();
    public abstract void reset();
    public abstract void create(Context context);
    public abstract Playback next(Context context);
    public abstract Playback previous(Context context);
    public abstract Song getCurrentSong();

    // 1.0f --> 100% volume
    public abstract void setVolume(float left, float right);
    public abstract boolean isCreated();
    public void setStartTime(int millis) {}
    public void setEndTime(int millis) {}

    public abstract String getTitle();
    public abstract String getSubtitle();
    public abstract String getMediaId();
}
