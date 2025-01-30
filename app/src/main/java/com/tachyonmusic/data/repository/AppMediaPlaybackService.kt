package com.tachyonmusic.data.repository

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Build
import androidx.media3.common.util.UnstableApi
import com.tachyonmusic.media.service.MediaPlaybackService
import com.tachyonmusic.presentation.entry.ActivityMain

@UnstableApi
class AppMediaPlaybackService : MediaPlaybackService() {
    override fun getSingleTopActivity(): PendingIntent? = getActivity(
        this,
        0,
        Intent(this, ActivityMain::class.java),
        immutableFlag or FLAG_UPDATE_CURRENT
    )

    override fun getBackStackedActivity(): PendingIntent? {
        val intent = Intent(this, ActivityMain::class.java).apply {
            action = ActivityMain.INTENT_ACTION_SHOW_PLAYER
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return getActivity(
            this,
            0,
            intent,
            immutableFlag or FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private val immutableFlag =
            if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0
    }
}