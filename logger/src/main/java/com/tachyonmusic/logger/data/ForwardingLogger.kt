package com.tachyonmusic.logger.data

import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

class ForwardingLogger : Logger {
    private val _exceptions = MutableSharedFlow<Pair<Throwable?, String?>>(
        200,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val exceptions = _exceptions.asSharedFlow()

    private val _strings =
        MutableSharedFlow<String>(200, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val strings = _strings.asSharedFlow()

    private val _uiTexts =
        MutableSharedFlow<Pair<UiText, String>?>(200, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val uiTexts = _uiTexts.asSharedFlow()

    override fun exception(e: Throwable?, message: String?) =
        runBlocking { _exceptions.emit(e to message) }

    override fun debug(message: String) = runBlocking { _strings.emit(message) }
    override fun info(message: String) = runBlocking { _strings.emit(message) }
    override fun warning(message: String) = runBlocking { _strings.emit(message) }
    override fun error(message: String) = runBlocking { _strings.emit(message) }

    override fun debug(message: UiText, prefix: String) =
        runBlocking { _uiTexts.emit(message to prefix) }

    override fun info(message: UiText, prefix: String) =
        runBlocking { _uiTexts.emit(message to prefix) }

    override fun warning(message: UiText, prefix: String) =
        runBlocking { _uiTexts.emit(message to prefix) }

    override fun error(message: UiText, prefix: String) =
        runBlocking { _uiTexts.emit(message to prefix) }
}