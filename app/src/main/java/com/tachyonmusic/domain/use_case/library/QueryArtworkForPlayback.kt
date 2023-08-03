package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.playback_layers.domain.ArtworkLoader

class QueryArtworkForPlayback(
    private val artworkLoader: ArtworkLoader
) {
    operator fun invoke(mediaId: MediaId, title: String, artist: String) = artworkLoader.findAllArtwork(
        mediaId,
        title,
        artist,
        pageSize = 4
    )
}