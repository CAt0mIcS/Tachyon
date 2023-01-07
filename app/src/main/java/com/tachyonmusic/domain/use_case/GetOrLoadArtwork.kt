package com.tachyonmusic.domain.use_case

import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.domain.repository.ArtworkCodex
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateInfo(
    val i: Int,
    val artwork: Artwork?
)

class GetOrLoadArtwork(
    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val artworkCodex: ArtworkCodex
) {

    suspend operator fun invoke(songs: List<SongEntity>) = channelFlow {
        withContext(Dispatchers.IO) {
            songs.forEachIndexed { i, entity ->
                launch {
                    if (!artworkCodex.isLoaded(entity.mediaId)) {
                        val res = artworkCodex.requestLoad(entity)
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
}