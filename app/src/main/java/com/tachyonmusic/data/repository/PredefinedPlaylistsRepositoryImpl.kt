package com.tachyonmusic.data.repository

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.domain.repository.PredefinedPlaylistsRepository
import com.tachyonmusic.domain.use_case.ObserveSettings
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.plus

class PredefinedPlaylistsRepositoryImpl(
    playbackRepository: PlaybackRepository,
    observeSettings: ObserveSettings,
    externalScope: CoroutineScope
) : PredefinedPlaylistsRepository {
    override var songPlaylist: List<SinglePlayback> = emptyList()
        private set
    override var loopPlaylist: List<SinglePlayback> = emptyList()
        private set

    init {
        combine(
            playbackRepository.songFlow,
            playbackRepository.loopFlow,
            observeSettings()
        ) { songs, loops, settings ->
            if (settings.combineDifferentPlaybackTypes) {
                songPlaylist = songs + loops
                loopPlaylist = loops + songs
            } else {
                songPlaylist = songs
                loopPlaylist = loops
            }
        }.launchIn(externalScope + Dispatchers.IO)
    }
}