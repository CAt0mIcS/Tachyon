package com.tachyonmusic.logger

import com.google.firebase.analytics.FirebaseAnalytics
import com.tachyonmusic.logger.data.ConsoleLogger
import com.tachyonmusic.logger.data.FirebaseAnalyticsLogger
import com.tachyonmusic.logger.data.FirebaseCrashlyticsLogger
import com.tachyonmusic.logger.domain.Logger

class Log(
    private val loggers: List<Logger> = listOf(
        ConsoleLogger(),
        FirebaseAnalyticsLogger(),
        FirebaseCrashlyticsLogger()
    )
) : Logger {
    object Event {
        const val SEARCH = FirebaseAnalytics.Event.SEARCH
        const val SELECT_CONTENT = FirebaseAnalytics.Event.SELECT_CONTENT
        const val SHARE = FirebaseAnalytics.Event.SHARE
        const val TUTORIAL_BEGIN = FirebaseAnalytics.Event.TUTORIAL_BEGIN
        const val TUTORIAL_COMPLETE = FirebaseAnalytics.Event.TUTORIAL_COMPLETE
        const val VIEW_SEARCH_RESULTS = FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS
        const val SELECT_ITEM = FirebaseAnalytics.Event.SELECT_ITEM
    }

    override fun debug(message: String) = each {
        debug(message)
    }

    override fun info(message: String) = each {
        info(message)
    }

    override fun warning(message: String) = each {
        warning(message)
    }

    override fun event(event: String, message: String) = each {
        event(event, message)
    }

    override fun exception(e: Throwable?, message: String?) = each {
        exception(e, message)
    }


    private fun each(action: Logger.() -> Unit) {
        loggers.forEach(action)
    }
}