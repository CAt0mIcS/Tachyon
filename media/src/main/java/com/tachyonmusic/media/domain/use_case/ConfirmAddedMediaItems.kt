package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.util.toPlayback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfirmAddedMediaItems(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val findPlaybackByMediaId: FindPlaybackByMediaId
) {
    suspend operator fun invoke(mediaItems: List<MediaItem>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItem>()
        for (item in mediaItems) {
            val mediaId = MediaId.deserializeIfValid(item.mediaId) ?: continue
            val playback =
                findPlaybackByMediaId(mediaId)?.toPlayback(songRepository, loopRepository)
            if (playback != null)
                list.add(playback.toMediaItem())
        }

        list
    }
}