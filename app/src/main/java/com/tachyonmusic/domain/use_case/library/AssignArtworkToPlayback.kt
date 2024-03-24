package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.repository.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssignArtworkToPlayback(
    private val songRepository: SongRepository
) {
    suspend operator fun invoke(mediaId: MediaId, artwork: Artwork) = invoke(
        mediaId,
        ArtworkType.getType(artwork),
        if (artwork is RemoteArtwork) artwork.uri.toURL().toString() else null
    )

    suspend operator fun invoke(mediaId: MediaId, artworkType: String, artworkUrl: String? = null) =
        withContext(Dispatchers.IO) {
            songRepository.updateArtwork(
                mediaId,
                artworkType,
                artworkUrl
            )
        }
}