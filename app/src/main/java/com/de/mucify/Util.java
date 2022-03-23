package com.de.mucify;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        Log.e("Mucify", msg);
    }


    public static String millisecondsToReadableString(int progress) {
        long millis = progress % 1000;
        long second = (progress / 1000) % 60;
        long minute = (progress / (1000 * 60)) % 60;
        long hour = (progress / (1000 * 60 * 60)) % 24;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d.%d", hour, minute, second, millis);
    }

    public static int requestAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        if(UserData.getIgnoreAudioFocus())
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(onChanged)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(UserData.getIgnoreAudioFocus() ? AudioAttributes.CONTENT_TYPE_SPEECH : AudioAttributes.CONTENT_TYPE_MUSIC)  // API 31 doesn't mute when playing with content_type_speech
                            .build())
                    .build();
            return audioManager.requestAudioFocus(audioFocusRequest);
        }

        return audioManager.requestAudioFocus(onChanged,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    public static int abandonAudioFocus(Context context, AudioManager.OnAudioFocusChangeListener onChanged) {
        if(UserData.getIgnoreAudioFocus())
            return 0;

        return ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(onChanged);
    }

    public static String getIPAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.getConnectionInfo() != null) {
            try {
                return InetAddress.getByAddress(
                        ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                                .putInt(wifiManager.getConnectionInfo().getIpAddress())
                                .array()
                ).getHostAddress();
            } catch (UnknownHostException e) {
                Log.e("Mucify", "Error finding IpAddress: " + e.getMessage());
            }
        }
        return null;
    }
}
