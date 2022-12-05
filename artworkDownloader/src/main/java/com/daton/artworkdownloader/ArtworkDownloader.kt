package com.daton.artworkdownloader

import com.daton.artworkdownloader.data.artwork_source.ITunesArtworkSource
import com.daton.artworkdownloader.domain.artwork_source.ArtworkSource
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.flow


class ArtworkDownloader(
    private val sources: List<ArtworkSource> = listOf(ITunesArtworkSource())
) {

    suspend fun query(title: String, artist: String, imageSize: Int) = flow {
        emit(Resource.Loading())

        for (source in sources) {
            emit(source.search(title, artist, imageSize))
        }
    }
}