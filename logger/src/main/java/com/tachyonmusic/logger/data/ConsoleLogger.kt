package com.tachyonmusic.logger.data

import android.util.Log
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.logger.util.CallerClassName

class ConsoleLogger : Logger {
    override fun debug(message: String) {
        Log.d(CallerClassName(), message)
    }

    override fun info(message: String) {
        Log.i(CallerClassName(), message)
    }

    override fun warning(message: String) {
        Log.w(CallerClassName(), message)
    }

    override fun event(event: String, message: String) {
        info("Event logged \"$event\": $message")
    }

    override fun exception(e: Throwable?, message: String?) {
        Log.e(
            CallerClassName(),
            "Exception occurred: ${e?.message.toString()}\n\tMessage: $message\n${e?.stackTraceToString() ?: "No Exception"}"
        )
    }
}