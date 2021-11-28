package com.de.mucify.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.de.mucify.R;
import com.de.mucify.playable.AudioController;
import com.de.mucify.service.MediaSessionService;


public class ForegroundNotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra("PlayPause") && intent.getAction().equals(MediaSessionService.ACTION_PLAY_PAUSE)) {
            if (intent.getIntExtra("PlayPause", 0) == R.drawable.ic_pause_black)
                AudioController.get().pauseSong();
            else if (intent.getIntExtra("PlayPause", 0) == R.drawable.ic_play_arrow_black)
                AudioController.get().unpauseSong();
        }
        else if(intent.getAction().equals(MediaSessionService.ACTION_PREVIOUS))
            AudioController.get().previous(context);
        else if(intent.getAction().equals(MediaSessionService.ACTION_NEXT))
            AudioController.get().next(context);
        else if(intent.getAction().equals("com.de.mucify.FOREGROUND_CLOSE")) {
            AudioController.get().reset();
        }
    }
}
