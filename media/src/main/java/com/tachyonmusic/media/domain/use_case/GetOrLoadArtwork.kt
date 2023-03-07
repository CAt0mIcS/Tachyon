package com.tachyonmusic.media.domain.use_case

import android.content.Context
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.domain.use_case.FindPlaybackByMediaId
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.media.domain.ArtworkCodex
import com.tachyonmusic.media.util.isPlayable
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateInfo(
    val i: Int,
    val artwork: Artwork?
)

class GetOrLoadArtwork(
    private val songRepository: SongRepository,
    private val settingsRepository: SettingsRepository,
    private val artworkCodex: ArtworkCodex,
    private val findPlayback: FindPlaybackByMediaId,
    private val isInternetMetered: GetIsInternetConnectionMetered,
    private val context: Context
) {

    @JvmName("invokePlaybackEntities")
    suspend operator fun invoke(songs: List<SongEntity>) = channelFlow {
        withContext(Dispatchers.IO) {
            val settings = settingsRepository.getSettings()
            val fetchOnline =
                settings.autoDownloadAlbumArtwork && !(settings.autoDownloadAlbumArtworkWifiOnly && isInternetMetered())

            songs.forEachIndexed { i, entity ->
                // Don't store unplayable artwork in codex so that it will load once it becomes playable
                if (!entity.mediaId.uri.isPlayable(context)) {
                    send(Resource.Success(UpdateInfo(i, artwork = null)))
                    return@forEachIndexed
                }


                launch {
                    val mediaId = entity.mediaId.underlyingMediaId ?: entity.mediaId
                    if (!artworkCodex.isLoaded(mediaId)) {
                        var res: Resource<SongEntity?>? = null
                        artworkCodex.awaitOrLoad(entity, fetchOnline).map {
                            if (it is Resource.Loading)
                                send(Resource.Loading(UpdateInfo(i, null)))

                            res = it
                        }.collect()
                        val entityToUpdate = res!!.data

                        if (res is Resource.Error)
                            send(Resource.Error(res as Resource.Error, UpdateInfo(i, null)))
                        else
                            send(Resource.Success(UpdateInfo(i, artworkCodex[mediaId])))

                        if (entityToUpdate != null)
                            updateArtwork(
                                songRepository,
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
    suspend operator fun invoke(songs: List<Song>) = channelFlow {
        withContext(Dispatchers.IO) {
            val entities = List(songs.size) { i ->
                findPlayback(songs[i].mediaId) as SongEntity?
            }.filterNotNull()

            invoke(entities).onEach {
                send(it)
            }.collect()
        }
    }

    @JvmName("invokePlayback")
    suspend operator fun invoke(song: Song) = invoke(listOf(song))

    @JvmName("invokePlaybackEntity")
    suspend fun invoke(entity: SongEntity) = invoke(listOf(entity))
}