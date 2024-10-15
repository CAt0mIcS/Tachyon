package com.tachyonmusic.metadata_api

import com.tachyonmusic.metadata_api.data.artwork_source.AmazonDigitalArtworkSource
import com.tachyonmusic.metadata_api.data.artwork_source.CoverArtArchiveArtworkSource
import com.tachyonmusic.metadata_api.data.artwork_source.ITunesArtworkSource
import com.tachyonmusic.metadata_api.domain.artwork_source.ArtworkSource
import com.tachyonmusic.metadata_api.domain.model.SearchInfo
import com.tachyonmusic.metadata_api.domain.model.isNullOrBlank
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.flow


class ArtworkFetcher(
    private val sources: List<ArtworkSource> = listOf(
        CoverArtArchiveArtworkSource(),
        ITunesArtworkSource(),
        AmazonDigitalArtworkSource()
    )
) {

    suspend fun query(info: SearchInfo, imageSize: Int, pageSize: Int = 1) = flow {
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

        if (info.isNullOrBlank()) {
            emit(Resource.Error(UiText.StringResource(R.string.invalid_media_metadata, info.toString())))
            return@flow
        }

        for (source in sources) {
            try {
                val result = source.search(info, imageSize, pageSize)
                emit(result)
            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(
                        UiText.build(
                            e.localizedMessage ?: R.string.unknown_encoder_error
                        )
                    )
                ) // TODO: No arg for unknown_encoder_error provided
            }
        }
    }
}