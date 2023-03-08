package com.tachyonmusic.artwork.data

import com.tachyonmusic.artwork.*
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.artwork.domain.GetIsInternetConnectionMetered
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Load artwork
class ArtworkMapperRepositoryImpl(
    permissionMapperRepository: PermissionMapperRepository,

    private val songRepository: SongRepository,
    private val settingsRepository: SettingsRepository,
    private val artworkCodex: ArtworkCodex,
    private val isInternetMetered: GetIsInternetConnectionMetered
) : ArtworkMapperRepository {

    override val songs = permissionMapperRepository.songs.map { songEntities ->
        val songs = songEntities.map { song ->
            song.toSong()
        }

        loadArtworkAsync(
            songs,
            songEntities.map { it.artworkType },
            songEntities.map { it.artworkUrl })

        songs
    }

    override val loops =
        combine(permissionMapperRepository.loops, songs) { loopEntities, songs ->
            loopEntities.mapNotNull { loop ->
                loop.toLoop(songs.find { it.mediaId == loop.mediaId.underlyingMediaId }
                    ?: return@mapNotNull null)
            }
        }

    override val playlists = permissionMapperRepository.playlists.map {
        it.map { playlist ->
            playlist.toPlaylist()
        }
    }

    override val history = permissionMapperRepository.history.map {
        it.map { history ->
            history.toSinglePlayback()
        }
    }


    private suspend fun loadArtworkAsync(
        items: List<Song>,
        artworkTypes: List<String>,
        artworkUrls: List<String?>
    ) = withContext(Dispatchers.IO) {
        launch {
            val settings = settingsRepository.getSettings()
            val fetchOnline =
                settings.autoDownloadAlbumArtwork && !(settings.autoDownloadAlbumArtworkWifiOnly && isInternetMetered())

            items.forEachIndexed { i, song ->
                // Don't store unplayable artwork in codex so that it will load once it becomes playable
                if (!song.isPlayable.value) {
                    return@forEachIndexed
                }


                launch {
                    val mediaId = song.mediaId.underlyingMediaId ?: song.mediaId
                    if (!artworkCodex.isLoaded(mediaId)) {
                        var res: Resource<SongPermissionEntity?>? = null
                        artworkCodex.awaitOrLoad(
                            song.toPermissionEntity(
                                artworkTypes[i],
                                artworkUrls[i]
                            ), fetchOnline
                        ).map {
                            if (it is Resource.Loading)
                                song.isArtworkLoading.update { true }

                            res = it
                        }.collect()
                        val entityToUpdate = res!!.data

                        if (res is Resource.Error)
                            song.artwork.update { null }
                        else
                            song.artwork.update { artworkCodex[mediaId] }

                        if (entityToUpdate != null)
                            updateArtwork(
                                songRepository,
                                entityToUpdate.toSongEntity(),
                                entityToUpdate.artworkType,
                                entityToUpdate.artworkUrl
                            )
                    } else {
                        song.artwork.update { artworkCodex[mediaId] }
                    }
                    song.isArtworkLoading.update { false }
                }
            }
        }
    }
}