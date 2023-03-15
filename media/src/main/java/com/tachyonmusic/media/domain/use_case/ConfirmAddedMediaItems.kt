package com.tachyonmusic.media.domain.use_case

import androidx.media3.common.MediaItem
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.toPlayback
import com.tachyonmusic.playback_layers.PlaybackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConfirmAddedMediaItems(
    private val playbackRepository: PlaybackRepository
) {
    suspend operator fun invoke(mediaItems: List<MediaItem>) = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItem>()

        val songs = playbackRepository.getSongs()
        val loops = playbackRepository.getLoops()

        for (item in mediaItems) {
            val mediaId = MediaId.deserializeIfValid(item.mediaId) ?: continue
            val playback = if (mediaId.isLocalSong)
                songs.find { it.mediaId == mediaId }
            else if (mediaId.isRemoteLoop)
                loops.find { it.mediaId == mediaId }
            else
                TODO("Invalid media id $mediaId")

            if (playback != null)
                list.add(playback.toMediaItem())
        }

        list
    }
}