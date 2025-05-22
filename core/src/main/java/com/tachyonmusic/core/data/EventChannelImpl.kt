package com.tachyonmusic.core.data

import com.tachyonmusic.core.domain.model.ChannelEvent
import com.tachyonmusic.core.domain.model.EventSeverity
import com.tachyonmusic.core.domain.model.EventType
import com.tachyonmusic.util.UiText
import com.tachyonmusic.core.domain.EventChannel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class EventChannelImpl : EventChannel {
    private val flow =
        MutableSharedFlow<ChannelEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    override fun listen(): SharedFlow<ChannelEvent> = flow

    override fun push(event: ChannelEvent) {
        assert(flow.tryEmit(event)) { "EventChannel flow was unable to emit" }
    }

    override fun push(message: UiText, severity: EventSeverity, eventType: EventType) =
        push(ChannelEvent(message, severity, eventType))
}