package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.playback_layers.PlaybackRepository

class FindPlaybackByMediaId(
    private val playbackRepository: PlaybackRepository
) {
    suspend operator fun invoke(mediaId: MediaId?): Playback? {
        if (mediaId == null)
            return null

        return when {
            mediaId.isLocalSong -> playbackRepository.getSongs().find { it.mediaId == mediaId }
            mediaId.isRemoteLoop -> playbackRepository.getLoops().find { it.mediaId == mediaId }
            else -> TODO("Invalid media id $mediaId")
        }
    }

}