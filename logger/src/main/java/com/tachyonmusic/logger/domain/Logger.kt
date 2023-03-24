package com.tachyonmusic.logger.domain

import com.tachyonmusic.util.UiText

interface Logger {
    fun exception(e: Throwable?, message: String? = null) {}

    fun debug(message: String) {}
    fun info(message: String) {}
    fun warning(message: String) {}
    fun error(message: String) {}

    fun debug(message: UiText, prefix: String = "") {}
    fun info(message: UiText, prefix: String = "") {}
    fun warning(message: UiText, prefix: String = "") {}
    fun error(message: UiText, prefix: String = "") {}
}