package com.tachyonmusic.media.domain.use_case

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateInfo(
    val i: Int,
    val artwork: Artwork?
)

class GetOrLoadArtwork(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val settingsRepository: SettingsRepository,
    private val artworkCodex: ArtworkCodex,
    private val findPlayback: FindPlaybackByMediaId,
    private val isInternetMetered: GetIsInternetConnectionMetered
) {

    @JvmName("invokePlaybackEntities")
    suspend operator fun invoke(songs: List<SinglePlaybackEntity>) = channelFlow {
        withContext(Dispatchers.IO) {
            val settings = settingsRepository.getSettings()
            val fetchOnline =
                !(!settings.autoDownloadAlbumArtwork || (settings.autoDownloadAlbumArtworkWifiOnly && isInternetMetered()))

            songs.forEachIndexed { i, entity ->
                launch {
                    val mediaId = entity.mediaId.underlyingMediaId ?: entity.mediaId
                    if (!artworkCodex.isLoaded(mediaId)) {
                        val res = artworkCodex.awaitOrLoad(entity, fetchOnline)
                        val entityToUpdate = res.data

                        if (res is Resource.Error)
                            send(Resource.Error(res, UpdateInfo(i, null)))
                        else
                            send(Resource.Success(UpdateInfo(i, artworkCodex[mediaId])))

                        if (entityToUpdate != null)
                            updateArtwork(
                                songRepository,
                                loopRepository,
                                entityToUpdate,
                                entityToUpdate.artworkType,
                                entityToUpdate.artworkUrl
                            )
                    } else {
                        send(Resource.Success(UpdateInfo(i, artworkCodex[mediaId])))
                    }
                }
            }
        }
    }

    @JvmName("invokePlaybacks")
    suspend operator fun invoke(playbacks: List<Playback>) = channelFlow {
        withContext(Dispatchers.IO) {
            val entities = List(playbacks.size) { i ->
                findPlayback(playbacks[i].mediaId) as SinglePlaybackEntity
            }

            invoke(entities).onEach {
                send(it)
            }.collect()
        }
    }

    @JvmName("invokePlayback")
    suspend operator fun invoke(playback: Playback) = invoke(listOf(playback))

    @JvmName("invokePlaybackEntity")
    suspend fun invoke(entity: SinglePlaybackEntity) = invoke(listOf(entity))
}