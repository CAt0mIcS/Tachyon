package com.tachyonmusic.database.domain.use_case

import android.content.Context
import com.tachyonmusic.artworkfetcher.ArtworkFetcher
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.database.domain.ArtworkSource
import com.tachyonmusic.database.domain.ArtworkType
import com.tachyonmusic.database.domain.model.PlaybackEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.ArtworkUpdateInfo
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
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
    private val appContext: Context,
    private val songRepo: SongRepository,
    private val loopRepo: LoopRepository,
    private val log: Logger = Log(),
    private val artworkFetcher: ArtworkFetcher = ArtworkFetcher()
) {
    suspend operator fun invoke(playback: PlaybackEntity?): ArtworkUpdateInfo? {
        if (playback !is SinglePlaybackEntity)
            return null

        return when (val artwork = artworkSource.get(playback)) {
            is RemoteArtwork -> ArtworkUpdateInfo(
                songRepo,
                loopRepo,
                playback,
                ArtworkType.REMOTE,
                artwork.uri.toURL().toString()
            )

            is EmbeddedArtwork -> ArtworkUpdateInfo(
                songRepo,
                loopRepo,
                playback,
                ArtworkType.EMBEDDED
            )
            null -> {
                /**
                 * We haven't yet found any artwork for this song, search the web if there's anything
                 * we can find (TODO: Don't hard-code imageSize 1000)
                 */
                log.info("Searching web for artwork for ${playback.title} - ${playback.artist}")
                var artworkInfo: ArtworkUpdateInfo? = null
                artworkFetcher.query(playback.title, playback.artist, 1000)
                    .onEach { res ->
                        if (res is Resource.Success)
                            artworkInfo = ArtworkUpdateInfo(
                                songRepo,
                                loopRepo,
                                playback,
                                ArtworkType.REMOTE,
                                res.data ?: return@onEach
                            )
                        else if (res is Resource.Error) {
                            log.exception(res.exception, res.message?.asString(appContext))
                        }
                    }.collect()
                artworkInfo
            }

            else -> TODO("Unknown artwork type")
        }
    }
}