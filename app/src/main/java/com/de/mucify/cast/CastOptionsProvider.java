package com.de.mucify.cast;

import android.content.Context;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import com.de.mucify.ui.ActivityPlayer;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.ArrayList;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    @NonNull
    @Override
    public CastOptions getCastOptions(@NonNull Context context) {
        List<String> buttonActions = new ArrayList<>();
        buttonActions.add(MediaIntentReceiver.ACTION_SKIP_PREV);
        buttonActions.add(MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK);
        buttonActions.add(MediaIntentReceiver.ACTION_SKIP_NEXT);
        buttonActions.add(MediaIntentReceiver.ACTION_STOP_CASTING);

        // Showing "play/pause" and "stop casting" in the compat view of the notification.
        int[] compatButtonActionsIndices = new int[]{1, 3};

        // Builds a notification with the above actions.
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(buttonActions, compatButtonActionsIndices)
                .setTargetActivityClassName(ActivityPlayer.class.getName())
                .build();

        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .build();

        return new CastOptions.Builder()
                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .setCastMediaOptions(mediaOptions)
                .build();
    }
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }
}
