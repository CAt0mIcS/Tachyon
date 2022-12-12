package com.tachyonmusic.logger.data

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.tachyonmusic.logger.domain.Logger

class FirebaseAnalyticsLogger : Logger {
    override fun event(event: String, message: String) {
        Firebase.analytics.logEvent(event, Bundle().apply {
            putString("message", message)
        })
    }
}