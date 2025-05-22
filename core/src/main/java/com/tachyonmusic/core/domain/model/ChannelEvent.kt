package com.tachyonmusic.core.domain.model

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.UiText

enum class EventSeverity {
    Debug, Info, Warning, Error, Fatal
}

sealed interface EventType {
    object Unknown: EventType
    object GenericError : EventType

    sealed interface MediaPlaybackService : EventType {
        class PlaybackIoErrorNotFound(val mediaId: MediaId?) : EventType.MediaPlaybackService
        class PlaybackIoErrorMissingPermission(val mediaId: MediaId?) : EventType.MediaPlaybackService
    }
}


class ChannelEvent(
    val message: UiText,
    val severity: EventSeverity,
    val eventType: EventType = EventType.Unknown
)