package com.de.mucify;

import android.content.Context;
import android.media.AudioManager;

import com.de.mucify.player.Playlist;
import com.de.mucify.player.Song;


public class Util {
    public static int requestAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return audioManager.requestAudioFocus(onChanged,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public static int abandonAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        return ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(onChanged);
    }

    public static String songMediaId(Song song) {
        return "Song_" + MediaLibrary.getSongIndex(song);
    }
    public static String loopMediaId(Song song) {
        return "Loop_" + MediaLibrary.getLoopIndex(song);
    }
    public static String playlistMediaId(Playlist playlist) {
        return "Playlist_" + MediaLibrary.getPlaylistIndex(playlist);
    }

    public static boolean isSongMediaId(String mediaId) {
        return mediaId.contains("Song_");
    }
    public static boolean isLoopMediaId(String mediaId) {
        return mediaId.contains("Loop_");
    }
    public static boolean isPlaylistMediaId(String mediaId) {
        return mediaId.contains("Playlist_");
    }

    public static int getIndexFromMediaId(String mediaId) {
        return Integer.parseInt(mediaId.substring(mediaId.lastIndexOf('_') + 1));
    }
}
