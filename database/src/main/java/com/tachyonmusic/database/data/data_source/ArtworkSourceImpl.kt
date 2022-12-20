package com.tachyonmusic.database.data.data_source

import com.tachyonmusic.database.domain.ArtworkSource
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import java.net.URI

class ArtworkSourceImpl : ArtworkSource {

    // TODO: Should we prefer already saved remote artwork or possibly new embedded artwork?
    override suspend fun get(playback: SinglePlaybackEntity): Artwork? =
        getRemoteArtwork(playback) ?: getEmbeddedArtwork(playback)


    private fun getEmbeddedArtwork(playback: SinglePlaybackEntity): EmbeddedArtwork? {
        val path = playback.mediaId.path
        if (path != null) {
            val embedded = EmbeddedArtwork.load(path)
            if (embedded != null) {
                return EmbeddedArtwork(embedded)
            }
        }
        return null
    }

    private fun getRemoteArtwork(playback: SinglePlaybackEntity): RemoteArtwork? {
        if (playback.artworkUrl?.isNotEmpty() == true) {
            return RemoteArtwork(URI(playback.artworkUrl!!))
        }
        return null
    }
}