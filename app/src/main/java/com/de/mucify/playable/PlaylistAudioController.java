package com.de.mucify.playable;

import android.widget.Toast;

import com.de.mucify.activity.MultiAudioActivity;
import com.de.mucify.activity.MultiAudioPlayActivity;

import java.util.ArrayList;

public class PlaylistAudioController {
    private static final PlaylistAudioController sInstance = new PlaylistAudioController();
    private Playlist mPlaylist;

    private final ArrayList<PlaylistResetListener> mPlaylistResetListeners = new ArrayList<>();

    public static PlaylistAudioController get() { return sInstance; }

    PlaylistAudioController() {
        AudioController.get().addOnSongFinishedListener(song -> {
            if(mPlaylist != null) {
                startPlaylist();
            }
        }, 0);
    }

    public String getPlaylistName() { return mPlaylist.getName(); }

    public void setPlaylist(Playlist playlist) {
        if(mPlaylist != null) {
            AudioController.get().reset();
            mPlaylist.reset();
        }
        mPlaylist = playlist;
    }

    public void startPlaylist() {
        Song song = mPlaylist.start();
        if(!AudioController.get().isSongNull())
            AudioController.get().pauseSong();
        AudioController.get().setSongNoReset(song);
        AudioController.get().startSong();
    }

    public void reset() {
        for(PlaylistResetListener listener : mPlaylistResetListeners)
            listener.onReset(mPlaylist);

        AudioController.get().reset();
        mPlaylist.reset();
        mPlaylist = null;
    }

    public int getSongCount() { return mPlaylist.getSongs().size(); }
    public boolean isPlaylistNull() { return mPlaylist == null; }
    public Song getCurrentSong() { return mPlaylist.getCurrentSong(); }

    public void addOnPlaylistResetListener(PlaylistResetListener listener, int i) {
        if(i == AudioController.INDEX_DONT_CARE)
            mPlaylistResetListeners.add(listener);
        else
            mPlaylistResetListeners.add(i, listener);
    }

    public interface PlaylistResetListener {
        void onReset(Playlist playlist);
    }
}
