package com.tachyonmusic.logger.data

import android.content.Context
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.UiText

class ConsoleUiTextLogger(
    private val appContext: Context
) : Logger {
    override fun debug(message: UiText, prefix: String) =
        debug(prefix + message.asString(appContext))

    override fun info(message: UiText, prefix: String) =
        info(prefix + message.asString(appContext))

    override fun warning(message: UiText, prefix: String) =
        warning(prefix + message.asString(appContext))

    override fun error(message: UiText, prefix: String) =
        error(prefix + message.asString(appContext))
}