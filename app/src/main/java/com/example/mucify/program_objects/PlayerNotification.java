package com.example.mucify.program_objects;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

import com.example.mucify.R;

public class PlayerNotification extends IntentService {
    private static final String NOTIFICATION_CHANNEL = "MucifyNotificationChannel";
    private static int FOREGROUND_ID = 1338;

    public PlayerNotification() {
        super("PlayerNotification");
    }

    @Override
    public void onHandleIntent(Intent i) {
        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (mgr.getNotificationChannel(NOTIFICATION_CHANNEL) == null) {
            NotificationChannel c = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "ForegroundPlayerNotification", NotificationManager.IMPORTANCE_DEFAULT);

            c.enableLights(true);
            c.setLightColor(0xFFFF0000);

            mgr.createNotificationChannel(c);
        }

        String songName = i.getDataString();

        Notification notification = buildForegroundNotification(songName);
        startForeground(FOREGROUND_ID, notification);
//        stopForeground(true);
    }

    private Notification buildForegroundNotification(String songName) {
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle("Playing Music")
                .setContentText(songName)
                .build();
    }
}
