package com.tachyonmusic.database.util

import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SongRepository

/**
 * Takes a [PlaybackEntity] and updates the artwork of the underlying song wherever it's saved.
 * So if we update a loop's artwork, the artwork for the song in the [SongRepository] is also set.
 */
suspend fun updateArtwork(
    songRepository: SongRepository,
    playback: PlaybackEntity?,
    artworkType: String,
    artworkUrl: String? = null
): Boolean {

    when (playback) {
        is SongEntity -> {
            songRepository.updateArtwork(playback, artworkType, artworkUrl)
        }

        is LoopEntity -> {
            songRepository.updateArtwork(
                songRepository.findByMediaId(
                    playback.mediaId.underlyingMediaId ?: return false
                ) ?: return false,
                artworkType,
                artworkUrl
            )
        }

        else -> return false
    }
    return true
}