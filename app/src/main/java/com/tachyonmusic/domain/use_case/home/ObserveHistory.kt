package com.tachyonmusic.domain.use_case.home

import com.tachyonmusic.playback_layers.domain.PlaybackRepository

class ObserveHistory(
    private val playbackRepository: PlaybackRepository
) {
    operator fun invoke() = playbackRepository.historyFlow
}