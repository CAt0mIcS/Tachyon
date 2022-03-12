package com.de.mucify;

import android.content.Context;
import android.media.AudioManager;


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
}
