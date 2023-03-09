package com.tachyonmusic.artwork.data

import com.tachyonmusic.artwork.*
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.artwork.domain.GetIsInternetConnectionMetered
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.database.util.updateArtwork
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.PlaylistPermissionEntity
import com.tachyonmusic.permission.domain.model.SinglePlaybackPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import com.tachyonmusic.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtworkMapperRepositoryImpl(
    private val permissionMapperRepository: PermissionMapperRepository,

    private val songRepository: SongRepository,
    private val settingsRepository: SettingsRepository,
    private val artworkCodex: ArtworkCodex,
    private val isInternetMetered: GetIsInternetConnectionMetered
) : ArtworkMapperRepository {

    override val songFlow = permissionMapperRepository.songFlow.map { songEntities ->
        transformSongs(songEntities)
    }

    override val loopFlow =
        combine(permissionMapperRepository.loopFlow, songFlow) { loopEntities, songs ->
            transformLoops(loopEntities, songs)
        }

    override val playlistFlow = combine(
        permissionMapperRepository.playlistFlow,
        songFlow,
        loopFlow
    ) { playlists, songs, loops ->
        transformPlaylists(playlists, songs, loops)
    }

    override val historyFlow = combine(
        permissionMapperRepository.historyFlow,
        songFlow,
        loopFlow
    ) { history, songs, loops ->
        transformHistory(history, songs, loops)
    }

    override suspend fun getSongs() = withContext(Dispatchers.IO) {
        transformSongs(permissionMapperRepository.getSongs())
    }

    override suspend fun getLoops() = withContext(Dispatchers.IO) {
        transformLoops(permissionMapperRepository.getLoops(), getSongs())
    }

    override suspend fun getPlaylists() = withContext(Dispatchers.IO) {
        transformPlaylists(permissionMapperRepository.getPlaylists(), getSongs(), getLoops())
    }

    override suspend fun getHistory() = withContext(Dispatchers.IO) {
        transformHistory(permissionMapperRepository.getHistory(), getSongs(), getLoops())
    }


    private suspend fun transformSongs(songEntities: List<SongPermissionEntity>) =
        withContext(Dispatchers.IO) {
            val songs = songEntities.map { song ->
                song.toSong()
            }

            loadArtworkAsync(
                songs,
                songEntities.map { it.artworkType },
                songEntities.map { it.artworkUrl })

            songs
        }

    private fun transformLoops(
        loopEntities: List<LoopPermissionEntity>,
        songs: List<Song>
    ): List<Loop> {
        return loopEntities.mapNotNull { loop ->
            loop.toLoop(songs.find { it.mediaId == loop.mediaId.underlyingMediaId }
                ?: return@mapNotNull null)
        }
    }

    /**
     * OPTIMIZE: Is it faster to always get [songs] and [loops] or is it faster
     *  to just load the artwork twice. Same goes for [transformHistory]
     */
    private fun transformPlaylists(
        playlists: List<PlaylistPermissionEntity>,
        songs: List<Song>,
        loops: List<Loop>
    ) = playlists.map { playlist ->
        playlist.toPlaylist(songs, loops)
    }

    private fun transformHistory(
        history: List<SinglePlaybackPermissionEntity>,
        songs: List<Song>,
        loops: List<Loop>
    ) = history.mapNotNull { historyItem ->
        historyItem.getFrom(songs, loops)
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