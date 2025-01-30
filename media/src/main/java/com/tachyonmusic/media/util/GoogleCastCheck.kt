package com.tachyonmusic.media.util

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun isGoogleCastAvailable(context: Context): Boolean {
    val isCastApiAvailable = isNotTv(context)
            && GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    try {
        CastContext.getSharedInstance(context)
    } catch (e: Exception) {
        return false
    }
    return isCastApiAvailable
}

private fun isNotTv(context: Context): Boolean {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return uiModeManager.currentModeType != Configuration.UI_MODE_TYPE_TELEVISION
}