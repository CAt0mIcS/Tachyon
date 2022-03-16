package com.de.mucify;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.de.mucify.player.Playback;
import com.de.mucify.service.MediaPlaybackService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;


public class Util {
    public static Thread.UncaughtExceptionHandler UncaughtExceptionLogger = (t, e) -> {
        String msg = e.getLocalizedMessage() + '\n' + Arrays.toString(e.getStackTrace());
        logGlobal(msg);
    };

    public static void logGlobal(String msg) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/mucify.log.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(msg + '\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String millisecondsToReadableString(int progress) {
        long millis = progress % 1000;
        long second = (progress / 1000) % 60;
        long minute = (progress / (1000 * 60)) % 60;
        long hour = (progress / (1000 * 60 * 60)) % 24;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%d", hour, minute, second, millis);
    }

    public static int requestAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        if(UserData.IgnoreAudioFocus)
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(onChanged)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(UserData.IgnoreAudioFocus ? AudioAttributes.CONTENT_TYPE_SPEECH : AudioAttributes.CONTENT_TYPE_MUSIC)  // API 31 doesn't mute when playing with content_type_speech
                            .build())
                    .build();
            return audioManager.requestAudioFocus(audioFocusRequest);
        }

        return audioManager.requestAudioFocus(onChanged,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public static int abandonAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        if(UserData.IgnoreAudioFocus)
            return 0;

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
            return MediaPlaybackService.Media.AvailableSongs.get(Util.getIndexFromMediaId(mediaId));
        }
        else if(Util.isLoopMediaId(mediaId)) {
            return MediaPlaybackService.Media.AvailableLoops.get(Util.getIndexFromMediaId(mediaId));
        }
        else if (Util.isPlaylistMediaId(mediaId)) {
            return MediaPlaybackService.Media.AvailablePlaylists.get(Util.getIndexFromMediaId(mediaId));
        }

        Log.e("Mucify: ", "Invalid media id " + mediaId);
        return null;
    }
}
