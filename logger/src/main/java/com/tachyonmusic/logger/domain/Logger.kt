package com.tachyonmusic.logger.domain

interface Logger {
    fun exception(e: Throwable?, message: String? = null) {}

    fun debug(message: String) {}
    fun info(message: String) {}
    fun warning(message: String) {}
    fun error(message: String) {}
}