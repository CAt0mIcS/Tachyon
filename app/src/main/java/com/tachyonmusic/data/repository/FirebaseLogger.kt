package com.tachyonmusic.data.repository

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.UiText
import dagger.hilt.android.qualifiers.ApplicationContext

class FirebaseLogger(
    @ApplicationContext private val appContext: Context
) : Logger {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun exception(e: Throwable?, message: String?) {
        crashlytics.recordException(e ?: return)
        crashlytics.log("[Exception] " + (message ?: return))
    }

    override fun info(message: String) {
        crashlytics.log("[Info] $message")
    }

    override fun info(message: UiText, prefix: String) {
        info(prefix + message.asString(appContext))
    }

    override fun warning(message: String) {
        crashlytics.log("[Warning] $message")
    }

    override fun warning(message: UiText, prefix: String) {
        warning(prefix + message.asString(appContext))
    }

    override fun error(message: String) {
        crashlytics.log("[Error] $message")
    }

    override fun error(message: UiText, prefix: String) {
        error(prefix + message.asString(appContext))
    }
}