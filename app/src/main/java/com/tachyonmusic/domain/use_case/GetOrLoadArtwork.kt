package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.domain.repository.ArtworkCodex
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
    private val artworkCodex: ArtworkCodex,
    private val findPlayback: FindPlaybackByMediaId
) {

    @JvmName("invokePlaybackEntities")
    suspend operator fun invoke(songs: List<SinglePlaybackEntity>) = channelFlow {
        withContext(Dispatchers.IO) {
            songs.forEachIndexed { i, entity ->
                launch {
                    if (!artworkCodex.isLoaded(entity.mediaId)) {
                        val res = artworkCodex.awaitOrLoad(entity)
                        val entityToUpdate = res.data

                        if (res is Resource.Error)
                            send(Resource.Error(res, UpdateInfo(i, null)))
                        else
                            send(Resource.Success(UpdateInfo(i, artworkCodex[entity.mediaId])))

                        if (entityToUpdate != null)
                            updateArtwork(
                                songRepository,
                                loopRepository,
                                entityToUpdate,
                                entityToUpdate.artworkType,
                                entityToUpdate.artworkUrl
                            )
                    } else {
                        send(Resource.Success(UpdateInfo(i, artworkCodex[entity.mediaId])))
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