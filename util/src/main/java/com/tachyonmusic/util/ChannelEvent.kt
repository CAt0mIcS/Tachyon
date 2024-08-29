package com.tachyonmusic.util

enum class EventSeverity {
    Debug, Info, Warning, Error, Fatal
}

interface ChannelEvent{
    val message: UiText
    val severity: EventSeverity
}