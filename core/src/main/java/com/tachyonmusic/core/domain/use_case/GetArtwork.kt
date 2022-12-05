package com.tachyonmusic.core.domain.use_case

import com.daton.artworkdownloader.ArtworkDownloader
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.net.URI

class GetArtwork(
    private val artworkDownloader: ArtworkDownloader = ArtworkDownloader()
) {
    suspend operator fun invoke(playback: SinglePlayback, imageSize: Int) =
        flow<Resource<Artwork>> {
            if (playback is LocalSongImpl) {
                val bitmap = EmbeddedArtwork.load(playback.path)
                if (bitmap != null) {
                    emit(Resource.Success(EmbeddedArtwork(bitmap)))
                    return@flow
                }
            }

            artworkDownloader.query(playback.title, playback.artist, imageSize).map {
                if (it is Resource.Error)
                    emit(Resource.Error(it.message))
                else if (it.data != null)
                    emit(Resource.Success(RemoteArtwork(URI(it.data!!))))
                else
                    emit(Resource.Loading())
            }.collect()
        }
}