package com.de.mucify.playable;

public class PlaylistAudioController {
    private static final PlaylistAudioController sInstance = new PlaylistAudioController();
    private Playlist mPlaylist;

    public static PlaylistAudioController get() { return sInstance; }

    PlaylistAudioController() {
        AudioController.get().addOnSongFinishedListener(song -> {
            if(mPlaylist != null) {
                mPlaylist.start();
            }
        }, AudioController.INDEX_DONT_CARE);
    }

    public void setPlaylist(Playlist playlist) {
        if(mPlaylist != null) {
            AudioController.get().reset();
            mPlaylist.reset();
        }
        mPlaylist = playlist;
    }

    public void startPlaylist() {
        mPlaylist.start();
        AudioController.get().startSong();
    }

    public void reset() {
        mPlaylist.reset();
        mPlaylist = null;
    }
}
