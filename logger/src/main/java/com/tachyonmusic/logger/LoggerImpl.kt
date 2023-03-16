package com.tachyonmusic.logger

import com.tachyonmusic.logger.data.ConsoleLogger
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.UiText

class LoggerImpl(
    private val loggers: List<Logger> = listOf(
        ConsoleLogger()
    )
) : Logger {
    override fun debug(message: String) = each { debug(message) }
    override fun info(message: String) = each { info(message) }
    override fun warning(message: String) = each { warning(message) }
    override fun error(message: String) = each { error(message) }

    override fun exception(e: Throwable?, message: String?) = each { exception(e, message) }

    override fun debug(message: UiText, prefix: String) = each { debug(message, prefix) }
    override fun info(message: UiText, prefix: String) = each { info(message, prefix) }
    override fun warning(message: UiText, prefix: String) = each { warning(message, prefix) }
    override fun error(message: UiText, prefix: String) = each { error(message, prefix) }


    private fun each(action: Logger.() -> Unit) {
        loggers.forEach(action)
    }
}