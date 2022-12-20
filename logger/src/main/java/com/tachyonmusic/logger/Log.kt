package com.tachyonmusic.logger

import com.tachyonmusic.logger.data.ConsoleLogger
import com.tachyonmusic.logger.domain.Logger

class Log(
    private val loggers: List<Logger> = listOf(
        ConsoleLogger()
    )
) : Logger {
    override fun debug(message: String) = each {
        debug(message)
    }

    override fun info(message: String) = each {
        info(message)
    }

    override fun warning(message: String) = each {
        warning(message)
    }

    override fun exception(e: Throwable?, message: String?) = each {
        exception(e, message)
    }


    private fun each(action: Logger.() -> Unit) {
        loggers.forEach(action)
    }
}