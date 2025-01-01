package com.tachyonmusic.presentation.util

import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.tachyonmusic.app.R


@Composable
fun AdmobNativeAppInstallAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background
    var nativeAdOut by remember { mutableStateOf<NativeAd?>(null) }

    val adLoader = remember {
        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")
            .forNativeAd { nativeAd ->
                // Ensure the ad is of type App Install Ad before displaying
                if (nativeAd.mediaContent != null && nativeAd.mediaContent!!.hasVideoContent()
                        .not() && nativeAd.headline != null && nativeAd.callToAction != null
                ) {
                    nativeAdOut = nativeAd
                } else {
                    nativeAd.destroy() // Clean up if ad doesn't fit your design
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdLoader", "Ad failed to load: ${adError.message}")
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT) // Customize AdChoices icon placement
                    .build()
            )
            .build()
    }

// Load an ad
    LaunchedEffect(Unit) {
        adLoader.loadAd(AdRequest.Builder().build())
    }

    if (nativeAdOut != null) {
        AndroidView(modifier = modifier, factory = { context ->
            val templateView = LayoutInflater.from(context)
                .inflate(R.layout.native_install_ad_layout, null, false) as TemplateView

            val styles = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(ColorDrawable(backgroundColor.value.toInt())).build()
            templateView.setStyles(styles);

            // Bind the NativeAd to the TemplateView
            nativeAdOut?.let {
                templateView.setNativeAd(it)
            }

            templateView
        })
    }

}