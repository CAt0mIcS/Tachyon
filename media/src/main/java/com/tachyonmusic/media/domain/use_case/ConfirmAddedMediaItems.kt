package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfirmAddedMediaItems(
    private val repository: UserRepository
) {
    suspend operator fun invoke(mediaItems: List<MediaItem>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItem>()
        for (item in mediaItems) {
            val playback = repository.find(MediaId.deserialize(item.mediaId))
            if (playback != null)
                list.add(playback.toMediaItem())
        }

        list
    }
}