package com.tachyonmusic.data.model

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.tachyonmusic.logger.domain.Logger
import dagger.hilt.android.qualifiers.ApplicationContext

class NativeInstallAdCache(@ApplicationContext context: Context, private val log: Logger) {
    private var nativeInstallAdCounter = -1
    private var nativeInstallAdLock = Any()
    val nativeAppInstallAdCache = mutableStateListOf<NativeAd>()

    private val nativeAppInstallAdLoader: AdLoader = AdLoader.Builder(
        context,
        NATIVE_INSTALL_AD_ID
    ).forNativeAd { nativeAd ->
        // Ensure the ad is of type App Install Ad before displaying

        synchronized(nativeInstallAdLock) {
//            if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()
//                    .not() && nativeAd.headline != null && nativeAd.callToAction != null
//            ) {
                nativeAppInstallAdCache.add(nativeAd)
                log.debug("[NativeAppInstallAdCache] Native Install Ad loaded")
//            } else {
//                nativeAd.destroy()
//                log.debug("[NativeAppInstallAdCache] Native Install Ad destroyed due to wrong type")
//            }

            nativeInstallAdCounter -= 1
        }

    }.withAdListener(object : AdListener() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
            log.error("[NativeAppInstallAdCache] Ad failed to load: ${adError.message}")
        }
    }).withNativeAdOptions(
        NativeAdOptions.Builder()
            .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT) // Customize AdChoices icon placement
            .build()
    ).build()

    fun loadNativeInstallAds() {
        synchronized(nativeInstallAdLock) {
            if (isLoadingNativeInstallAd || nativeAppInstallAdCache.size == 10)
                return

            log.debug("[NativeAppInstallAdCache] Loading 5 native install ads...")
            nativeInstallAdCounter = 4
            nativeAppInstallAdLoader.loadAds(AdRequest.Builder().build(), 5)
        }

        // TODO: Reload ads after 60.min
    }

    fun unloadNativeInstallAds() {
        synchronized(nativeInstallAdLock) {
            for (it in nativeAppInstallAdCache) {
                it.destroy()
                nativeAppInstallAdCache.remove(it)
            }
            log.debug("[NativeAppInstallAdCache] Unloaded native app install ads")
        }
    }

    val isLoadingNativeInstallAd: Boolean
        get() = nativeInstallAdCounter != -1

    companion object {
        const val NATIVE_INSTALL_AD_TEST_ID = "ca-app-pub-3940256099942544/2247696110"
        const val NATIVE_INSTALL_AD_ID = "ca-app-pub-7145716621236451/6099968554"
    }
}