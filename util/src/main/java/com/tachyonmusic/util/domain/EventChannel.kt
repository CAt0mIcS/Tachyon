package com.tachyonmusic.util.domain

import com.tachyonmusic.util.ChannelEvent
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.SharedFlow

interface EventChannel {
    fun listen(): SharedFlow<ChannelEvent>
    fun push(event: ChannelEvent)
    fun push(message: UiText, severity: EventSeverity)
}
