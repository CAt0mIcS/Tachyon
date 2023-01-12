package com.tachyonmusic.database.domain.use_case

import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toPlayback

class ConvertHistoryEntityToPlayback(
    private val findPlaybackByMediaId: FindPlaybackByMediaId,
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository
) {
    suspend operator fun invoke(history: HistoryEntity?): Playback? {
        if (history == null)
            return null

        return findPlaybackByMediaId(history.mediaId)?.toPlayback(songRepository, loopRepository)
    }
}