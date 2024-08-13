package com.tachyonmusic.presentation.util

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun AdmobBanner(modifier: Modifier = Modifier, optionalAdView: AdView? = null, onBannerHeight: (Int) -> Unit = {}, onNewAdViewCreated: (AdView) -> Unit = {}) {

//    BoxWithConstraints(modifier = modifier) {
//        val screenWidthPx = with(LocalDensity.current) {
//            maxWidth.toPx()
//        }
//        val density = LocalDensity.current.density
//
//        AndroidView(
//            factory = { context ->
//                val cachedOrNewView = optionalAdView ?: AdView(context)
//                if(optionalAdView == null)
//                    onNewAdViewCreated(cachedOrNewView)
//
//
//                optionalAdView ?: cachedOrNewView.apply {
//                    var adWidthPixels = width.toFloat()
//                    if (adWidthPixels == 0f) {
//                        adWidthPixels = screenWidthPx
//                    }
//
//                    val adWidth = (adWidthPixels / density).toInt()
//                    val widthFilledAdSize =
//                        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
//
//                    setAdSize(widthFilledAdSize)
//
//                    adListener = object : AdListener() {
//                        override fun onAdFailedToLoad(p0: LoadAdError) {
//                            super.onAdFailedToLoad(p0)
//                            onBannerHeight(0) // TODO: Not working (Layout in MiniPlayerScreen never gets recomposed)
//                        }
//                    }
//
//                    // Test-key
////                    adUnitId = "ca-app-pub-3940256099942544/9214589741"
//                    adUnitId = "ca-app-pub-7145716621236451/1719814074"
//                    loadAd(AdRequest.Builder().build())
//                    onBannerHeight(widthFilledAdSize.height + 12)
//                }
//            }
//        )
//    }
}