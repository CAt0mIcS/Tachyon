package com.daton.database.data.repository.shared_action

import android.util.Log
import com.daton.artworkfetcher.ArtworkFetcher
import com.daton.database.domain.ArtworkSource
import com.daton.database.domain.ArtworkType
import com.daton.database.domain.model.PlaybackEntity
import com.daton.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * Takes a playback and tries to get already stored artwork in [SinglePlaybackEntity.artworkType].
 * If this fails it'll try to get artwork from the [ArtworkFetcher] which might find something
 * on the internet
 */
class LoadArtwork(
    private val artworkSource: ArtworkSource,
    private val updateArtwork: UpdateArtwork,
    private val artworkFetcher: ArtworkFetcher = ArtworkFetcher()
) {
    suspend operator fun invoke(playback: PlaybackEntity?) {
        if (playback !is SinglePlaybackEntity)
            return

        when (val artwork = artworkSource.get(playback)) {
            is RemoteArtwork -> updateArtwork(
                playback,
                ArtworkType.REMOTE,
                artwork.uri.toURL().toString()
            )
            is EmbeddedArtwork -> updateArtwork(playback, ArtworkType.EMBEDDED)
            null -> {
                /**
                 * We haven't yet found any artwork for this song, search the web if there's anything
                 * we can find
                 */
                Log.d(
                    "LoadArtwork",
                    "Searching web for artwork for ${playback.title} - ${playback.artist}"
                )
                artworkFetcher.query(playback.title, playback.artist, 1000)
                    .onEach { res ->
                        if (res is Resource.Success)
                            updateArtwork(playback, ArtworkType.REMOTE, res.data ?: return@onEach)
                    }.collect()
            }
            else -> TODO("Unknown artwork type")
        }
    }
}