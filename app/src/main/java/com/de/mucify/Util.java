package com.de.mucify;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.de.mucify.player.Playback;
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

    public static Playback getPlaybackFromMediaId(String mediaId) {
        if(Util.isSongMediaId(mediaId)) {
            return MediaLibrary.AvailableSongs.get(Util.getIndexFromMediaId(mediaId));
        }
        else if(Util.isLoopMediaId(mediaId)) {
            return MediaLibrary.AvailableLoops.get(Util.getIndexFromMediaId(mediaId));
        }
        else if (Util.isPlaylistMediaId(mediaId)) {
            return MediaLibrary.AvailablePlaylists.get(Util.getIndexFromMediaId(mediaId));
        }

        Log.e("Mucify: ", "Invalid media id " + mediaId);
        return null;
    }
}
