package com.de.mucify.service;

import android.content.Context;

import androidx.annotation.NonNull;

import com.de.mucify.R;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.ArrayList;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    public static final String CUSTOM_NAMESPACE = "urn:x-cast:com.de.mucify.CAST";

    @NonNull
    @Override
    public CastOptions getCastOptions(@NonNull Context context) {
        List<String> supportedNamespaces = new ArrayList<>();
        supportedNamespaces.add(CUSTOM_NAMESPACE);

        return new CastOptions.Builder()
//                .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .setReceiverApplicationId("C0868879")
//                .setSupportedNamespaces(supportedNamespaces)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(@NonNull Context context) {
        return null;
    }
}
