package com.daton.database.data.data_source

import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.model.SinglePlaybackEntity
import com.daton.database.domain.repository.SettingsRepository
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.Artwork
import java.io.File
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
        if (playback.artwork?.isNotEmpty() == true) {
            return RemoteArtwork(URI(playback.artwork!!))
        }
        return null
    }
}