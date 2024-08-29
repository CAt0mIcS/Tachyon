package com.tachyonmusic.util.domain

import com.tachyonmusic.util.ChannelEvent
import kotlinx.coroutines.flow.SharedFlow

interface EventChannel {
    fun listen(): SharedFlow<ChannelEvent>
    fun push(event: ChannelEvent)
}
