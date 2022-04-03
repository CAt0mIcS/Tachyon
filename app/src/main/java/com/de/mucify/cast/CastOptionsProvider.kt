package com.de.mucify.cast

import android.content.Context
import com.google.android.exoplayer2.ext.cast.DefaultCastOptionsProvider
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        return CastOptions.Builder()
            // Use the Default Media Receiver with DRM support.
            .setReceiverApplicationId(DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM)
            .setCastMediaOptions(
                CastMediaOptions.Builder()
                    // We manage the media session and the notifications ourselves.
                    .setMediaSessionEnabled(false)
                    .setNotificationOptions(null)
                    .build()
            )
            .setStopReceiverApplicationWhenEndingSession(true).build()
    }

    override fun getAdditionalSessionProviders(context: Context): List<SessionProvider?>? {
        return null
    }
}