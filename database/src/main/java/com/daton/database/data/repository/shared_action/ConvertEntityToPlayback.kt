package com.daton.database.data.repository.shared_action

import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.PlaylistEntity
import com.daton.database.domain.model.SongEntity
import com.tachyonmusic.core.domain.playback.Playback

class ConvertEntityToPlayback(
    private val toSong: ConvertEntityToSong,
    private val toLoop: ConvertEntityToLoop,
    private val toPlaylist: ConvertEntityToPlaylist
) {
    suspend operator fun invoke(playback: PlaybackEntity): Playback =
        when (playback) {
            is SongEntity -> toSong(playback)
            is LoopEntity -> toLoop(playback)
            is PlaylistEntity -> toPlaylist(playback)
            else -> TODO("Invalid playback type ${playback::class.java.name}")
        }
}