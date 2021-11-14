package com.de.mucify.playable;

public class PlaylistAudioController {
    private static final PlaylistAudioController sInstance = new PlaylistAudioController();
    private Playlist mPlaylist;

    public static PlaylistAudioController get() { return sInstance; }

    PlaylistAudioController() {
        AudioController.get().addOnSongFinishedListener(song -> {
            if(mPlaylist != null) {
                startPlaylist();
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
        Song song = mPlaylist.start();
        if(!AudioController.get().isSongNull())
            AudioController.get().pauseSong();
        AudioController.get().setSongNoReset(song);
        AudioController.get().startSong();
    }

    public void reset() {
        AudioController.get().reset();
        mPlaylist.reset();
        mPlaylist = null;
    }
}
