package com.tachyonmusic.database.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository

class FindPlaybackByMediaId(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository
) {
    suspend operator fun invoke(mediaId: MediaId?): PlaybackEntity? {
        if (mediaId == null)
            return null

        return songRepository.findByMediaId(mediaId) ?: loopRepository.findByMediaId(mediaId)
        ?: playlistRepository.findByMediaId(mediaId)
    }

}