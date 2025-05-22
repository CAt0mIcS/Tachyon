package com.tachyonmusic.core.domain

import com.tachyonmusic.core.domain.model.ChannelEvent
import com.tachyonmusic.core.domain.model.EventSeverity
import com.tachyonmusic.core.domain.model.EventType
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.SharedFlow

interface EventChannel {
    fun listen(): SharedFlow<ChannelEvent>
    fun push(event: ChannelEvent)
    fun push(message: UiText, severity: EventSeverity, eventType: EventType = EventType.Unknown)
}