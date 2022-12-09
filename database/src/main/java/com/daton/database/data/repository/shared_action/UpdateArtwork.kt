package com.daton.database.data.repository.shared_action

import com.daton.database.domain.model.LoopEntity
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.SongEntity
import com.daton.database.domain.repository.HistoryRepository
import com.daton.database.domain.repository.LoopRepository
import com.daton.database.domain.repository.SongRepository

/**
 * Takes a [PlaybackEntity] and updates the artwork of the underlying song wherever it's saved.
 * So if we update a loop's artwork, the artwork for the song in the [SongRepository] is also set.
 * Same goes for the history repository
 */
class UpdateArtwork(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val historyRepository: HistoryRepository // TODO!!!
) {
    suspend operator fun invoke(
        playback: PlaybackEntity?,
        artworkType: String,
        artworkUrl: String? = null
    ) {

        when (playback) {
            is SongEntity -> {
                songRepository.updateArtwork(playback, artworkType, artworkUrl)

                val loopWithSong = loopRepository.findBySong(playback)
                if (loopWithSong != null)
                    loopRepository.updateArtwork(loopWithSong, artworkType, artworkUrl)
            }
            is LoopEntity -> {
                loopRepository.updateArtwork(playback, artworkType, artworkUrl)

                // Should never be null
                val songOfLoop =
                    songRepository.findByMediaId(playback.mediaId.underlyingMediaId ?: return)
                if (songOfLoop != null)
                    songRepository.updateArtwork(songOfLoop, artworkType, artworkUrl)
            }
            else -> {}
        }

    }
}