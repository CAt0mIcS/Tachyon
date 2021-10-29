package com.mucify.ui.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Action", Toast.LENGTH_LONG).show();
//        if (!Intent.ACTION_MEDIA_BUTTON.equals(intent))
//            return;
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null)
            return;

        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            switch(event.getKeyCode()) {

            }
        }
        abortBroadcast();
    }
}
