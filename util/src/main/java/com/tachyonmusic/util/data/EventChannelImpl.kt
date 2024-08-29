package com.tachyonmusic.util.data

import com.tachyonmusic.util.ChannelEvent
import com.tachyonmusic.util.domain.EventChannel
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
}