package com.tachyonmusic.playback_layers.domain.events

import com.tachyonmusic.util.ChannelEvent
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText

class PlaybackNotFoundEvent(override val message: UiText, override val severity: EventSeverity) :
    ChannelEvent