package com.daton.artworkfetcher

import com.daton.artworkfetcher.data.artwork_source.AmazonDigitalArtworkSource
import com.daton.artworkfetcher.data.artwork_source.ITunesArtworkSource
import com.daton.artworkfetcher.domain.artwork_source.ArtworkSource
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.flow


class ArtworkFetcher(
    private val sources: List<ArtworkSource> = listOf(
        ITunesArtworkSource(),
        AmazonDigitalArtworkSource()
    )
) {

    suspend fun query(title: String, artist: String, imageSize: Int) = flow {
        emit(Resource.Loading())

        if (imageSize <= 0) {
            emit(
                Resource.Error(
                    UiText.StringResource(
                        R.string.invalid_image_size,
                        imageSize.toString()
                    )
                )
            )
            return@flow
        }

        if (title.isBlank() || artist.isBlank()) {
            emit(
                Resource.Error(
                    UiText.StringResource(
                        R.string.invalid_media_metadata,
                        title,
                        artist
                    )
                )
            )
            return@flow
        }

        for (source in sources) {
            val result = source.search(title, artist, imageSize)
            emit(result)
            if (result is Resource.Success)
                break
        }
    }
}