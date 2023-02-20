package com.tachyonmusic.database.domain.use_case

import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.util.toPlayback

class ConvertHistoryEntityToPlayback(
    private val findPlaybackByMediaId: FindPlaybackByMediaId
) {
    suspend operator fun invoke(history: HistoryEntity?): SinglePlayback? {
        if (history == null)
            return null

        return when (val playbackEntity = findPlaybackByMediaId(history.mediaId)) {
            is SinglePlaybackEntity? -> playbackEntity?.toPlayback()
            else -> null
        }
    }
}