package com.de.mucify.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.de.mucify.playable.AudioController;

public class BecomingNoisyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            if(!AudioController.get().isSongNull())
                AudioController.get().pauseSong();
        }
    }
}
