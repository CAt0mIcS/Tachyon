package com.tachyonmusic.logger.data

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.tachyonmusic.logger.domain.Logger

class FirebaseCrashlyticsLogger : Logger {
    override fun exception(e: Throwable?, message: String?) {
        if (message != null)
            Firebase.crashlytics.setCustomKeys {
                key("message", message)
            }
        Firebase.crashlytics.recordException(e ?: Exception("Unknown Exception"))
    }

    override fun error(message: String) {
        exception(null, message)
    }
}