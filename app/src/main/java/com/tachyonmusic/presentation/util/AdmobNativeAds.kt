package com.tachyonmusic.presentation.util

import android.view.LayoutInflater
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.nativead.NativeAd
import com.tachyonmusic.app.R


@Composable
fun AdmobSmallNativeAd(modifier: Modifier = Modifier, nativeAd: NativeAd?) {
    val backgroundColor = MaterialTheme.colorScheme.background

    if (nativeAd != null) {
        AndroidView(modifier = modifier, factory = { context ->
            val templateView = LayoutInflater.from(context)
                .inflate(R.layout.native_install_ad_layout, null, false) as TemplateView

            val styles = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(backgroundColor.value.toInt().toDrawable()).build()
            templateView.setStyles(styles)

            // Bind the NativeAd to the TemplateView
            templateView.setNativeAd(nativeAd)

            templateView
        })
    }
}

@Composable
fun AdmobMediumNativeAd(modifier: Modifier = Modifier, nativeAd: NativeAd?) {
    val backgroundColor = MaterialTheme.colorScheme.background

    if (nativeAd != null) {
        AndroidView(modifier = modifier, factory = { context ->
            val templateView = LayoutInflater.from(context)
                .inflate(R.layout.native_advanced_big_ad_layout, null, false) as TemplateView

            val styles = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(backgroundColor.value.toInt().toDrawable()).build()
            templateView.setStyles(styles)

            // Bind the NativeAd to the TemplateView
            templateView.setNativeAd(nativeAd)

            templateView
        })
    }
}