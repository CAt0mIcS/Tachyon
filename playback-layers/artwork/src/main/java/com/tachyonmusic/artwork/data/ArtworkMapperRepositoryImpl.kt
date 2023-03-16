package com.tachyonmusic.artwork.data

import com.tachyonmusic.artwork.*
import com.tachyonmusic.artwork.domain.ArtworkCodex
import com.tachyonmusic.artwork.domain.ArtworkMapperRepository
import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.SinglePlayback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.PlaylistPermissionEntity
import com.tachyonmusic.permission.domain.model.SinglePlaybackPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


class ArtworkMapperRepositoryImpl(
    private val permissionMapperRepository: PermissionMapperRepository,
    private val artworkCodex: ArtworkCodex
) : ArtworkMapperRepository {

    private val reloadSongs = MutableStateFlow(false)

    override val songFlow =
        combine(permissionMapperRepository.songFlow, reloadSongs) { songEntities, _ ->
            transformSongs(songEntities)
        }

    override val loopFlow =
        combine(permissionMapperRepository.loopFlow, songFlow) { loopEntities, songs ->
            transformLoops(loopEntities, songs)
        }

    override val playlistFlow =
        combine(permissionMapperRepository.playlistFlow, songFlow) { playlists, songs ->
            transformPlaylists(playlists, songs)
        }

    override val historyFlow =
        combine(permissionMapperRepository.historyFlow, songFlow) { history, songs ->
            transformHistory(history, songs)
        }

    override suspend fun getSongs() = withContext(Dispatchers.IO) {
        transformSongs(permissionMapperRepository.getSongs())
    }

    override suspend fun getLoops() = withContext(Dispatchers.IO) {
        transformLoops(permissionMapperRepository.getLoops(), getSongs())
    }

    override suspend fun getPlaylists() = withContext(Dispatchers.IO) {
        transformPlaylists(permissionMapperRepository.getPlaylists(), getSongs())
    }

    override suspend fun getHistory() = withContext(Dispatchers.IO) {
        transformHistory(permissionMapperRepository.getHistory(), getSongs())
    }


    override fun triggerSongReload() {
        reloadSongs.update { !it }
    }


    private fun transformSongs(songEntities: List<SongPermissionEntity>) =
        songEntities.map { songEntity ->
            songEntity.toSong().applyArtwork()
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
        songs: List<Song>
    ) = playlists.map { playlist ->
        playlist.toPlaylist(playlist.items.mapNotNull {
            it.toSinglePlayback(songs)
        })
    }

    private fun transformHistory(
        history: List<SinglePlaybackPermissionEntity>,
        songs: List<Song>
    ) = history.mapNotNull {
        it.toSinglePlayback(songs)
    }


    private fun SinglePlaybackPermissionEntity.toSinglePlayback(songs: List<Song>): SinglePlayback? {
        return when (this) {
            is SongPermissionEntity -> {
                toSong().applyArtwork()
            }

            is LoopPermissionEntity -> {
                toLoop(
                    songs.find { it.mediaId == mediaId.underlyingMediaId }
                        ?.applyArtwork()
                        ?: return null
                )
            }
            else -> TODO("Invalid permission entity type ${this::class.java.name}")
        }
    }

    private fun Song.applyArtwork(): Song {
        if (artworkCodex.isLoaded(mediaId)) {
            artwork.update { artworkCodex[mediaId] }
            isArtworkLoading.update { false }
        } else
            isArtworkLoading.update { true }
        return this
    }
}