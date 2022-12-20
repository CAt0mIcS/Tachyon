package com.daton.database.domain.use_case

import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.PlaylistRepository
import com.daton.database.domain.repository.SongRepository
import com.tachyonmusic.core.domain.MediaId

class FindPlaybackByMediaId(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(mediaId: MediaId) =
        songRepository.findByMediaId(mediaId) ?: loopRepository.findByMediaId(mediaId)
        ?: playlistRepository.findByMediaId(mediaId)
}