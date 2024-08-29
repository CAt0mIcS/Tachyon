package com.tachyonmusic.domain.use_case.library

import com.tachyonmusic.playback_layers.domain.ArtworkLoader
import com.tachyonmusic.presentation.library.model.LibraryEntity

class QueryArtworkForPlayback(
    private val artworkLoader: ArtworkLoader
) {
    operator fun invoke(playback: LibraryEntity, searchQuery: String? = null) =
        artworkLoader.findAllArtwork(
            playback.mediaId,
            if (searchQuery.isNullOrBlank()) "${playback.artist} ${playback.title}" else searchQuery,
            pageSize = 4
        )
}