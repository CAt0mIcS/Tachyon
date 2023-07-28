package com.tachyonmusic.playback_layers.data

import android.content.Context
import com.tachyonmusic.playback_layers.domain.ArtworkCodex
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.*
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.CustomizedSongEntity
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.CustomizedSongRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.checkIfPlayable
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.SortingPreferences
import com.tachyonmusic.playback_layers.sortedBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.plus
import java.net.URI

class PlaybackRepositoryImpl(
    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,

    private val artworkCodex: ArtworkCodex,

    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val log: Logger
) : PlaybackRepository {

    private val _sortingPreferences = MutableStateFlow(SortingPreferences())
    override val sortingPreferences = _sortingPreferences.asStateFlow()

    override val songFlow =
        combine(songRepository.observe(), sortingPreferences) { songEntities, sorting ->
            transformSongs(songEntities, sorting)
        }

    override val customizedSongFlow =
        combine(
            customizedSongRepository.observe(),
            songFlow,
            sortingPreferences
        ) { customizedSongEntities, songs, sorting ->
            transformCustomizedSongs(customizedSongEntities, songs, sorting)
        }

    override val playlistFlow = combine(
        playlistRepository.observe(),
        songFlow,
        customizedSongFlow,
        sortingPreferences
    ) { playlistEntities, songs, customizedSongs, sorting ->
        transformPlaylists(playlistEntities, songs, customizedSongs, sorting)
    }

    override val historyFlow = combine(
        historyRepository.observe(),
        songFlow,
        customizedSongFlow
    ) { historyEntities, songs, customizedSongs ->
        transformHistory(historyEntities, songs, customizedSongs)
    }

    override suspend fun getSongs() =
        transformSongs(songRepository.getSongs(), sortingPreferences.value)

    override suspend fun getCustomizedSongs() =
        transformCustomizedSongs(
            customizedSongRepository.getCustomizedSongs(),
            getSongs(),
            sortingPreferences.value
        )

    override suspend fun getPlaylists() =
        transformPlaylists(
            playlistRepository.getPlaylists(),
            getSongs(),
            getCustomizedSongs(),
            sortingPreferences.value
        )

    override suspend fun getHistory() =
        transformHistory(historyRepository.getHistory(), getSongs(), getCustomizedSongs())

    override fun setSortingPreferences(sortPrefs: SortingPreferences) {
        _sortingPreferences.update { sortPrefs }
    }

    private suspend fun transformSongs(entities: List<SongEntity>, sorting: SortingPreferences) =
        entities.map { entity ->
            if (entity.mediaId.isLocalSong) {

                val artwork = if (entity.artworkType == ArtworkType.UNKNOWN) {
                    /**
                     * Load new artwork for [entity] if artwork is not cached in the database
                     */
                    artworkCodex.awaitOrLoad(entity).onEach {
                        val entityToUpdate = it.data?.entityToUpdate
                        maybeUpdateEntityArtwork(entityToUpdate)

                        log.warning(
                            prefix = "ArtworkLoader error on ${entityToUpdate?.title} - ${entityToUpdate?.artist}: ",
                            message = it.message ?: return@onEach
                        )
                    }.launchIn(coroutineScope + Dispatchers.IO)
                    null
                } else {
                    /**
                     * Artwork is already cached in the database and we just need to load it and set it
                     * in the song constructor
                     */
                    val artworkUpdateData = artworkCodex.loadExisting(entity)
                    maybeUpdateEntityArtwork(artworkUpdateData.entityToUpdate)
                    artworkUpdateData.artwork
                }

                LocalSong(
                    entity.mediaId.uri!!,
                    entity.mediaId,
                    entity.title,
                    entity.artist,
                    entity.duration
                ).let {
                    it.isPlayable = entity.checkIfPlayable(context)
                    it.isArtworkLoading = entity.artworkType == ArtworkType.UNKNOWN
                    it.artwork = artwork
                    it
                }
            } else if (entity.mediaId.isSpotifySong) {
                SpotifySong(entity.mediaId, entity.title, entity.artist, entity.duration).let {
                    it.isPlayable = true
                    it.artwork = RemoteArtwork(URI(entity.artworkUrl))
                    it
                }
            } else
                TODO("Invalid media id ${entity.mediaId}")
        }.sortedBy(sorting)

    private fun transformCustomizedSongs(
        entities: List<CustomizedSongEntity>,
        songs: List<Song>,
        sorting: SortingPreferences
    ): List<CustomizedSong> =
        entities.map { entity ->
            LocalCustomizedSong(
                entity.mediaId,
                songs.find { it.mediaId == entity.mediaId.underlyingMediaId }
                    ?: TODO("Invalid song media id ${entity.mediaId.underlyingMediaId}")
            ).let {
                it.timingData = entity.timingData?.let {
                    TimingDataController(
                        it,
                        entity.currentTimingDataIndex
                    )
                }
                it.bassBoost = entity.bassBoost
                it.virtualizerStrength = entity.virtualizerStrength
                it.equalizerBands = entity.equalizerBands
                it.playbackParameters = entity.playbackParameters
                it.reverb = entity.reverb
                it
            }
        }.sortedBy(sorting)

    private fun transformPlaylists(
        entities: List<PlaylistEntity>,
        songs: List<Song>,
        customizedSongs: List<CustomizedSong>,
        sorting: SortingPreferences
    ) = entities.map { entity ->
        val items = entity.items.mapNotNull { playlistItem ->
            if (playlistItem.isLocalSong || playlistItem.isSpotify) {
                songs.find { playlistItem == it.mediaId }
            } else if (playlistItem.isLocalCustomizedSong) {
                customizedSongs.find { playlistItem == it.mediaId }
            } else {
                TODO("Invalid playlist item $playlistItem")
            }
        }

        if (entity.mediaId.isSpotifyPlaylist) {
            SpotifyPlaylist(
                entity.name,
                entity.mediaId,
                items.map { it as SpotifySong }.toMutableList(),
                entity.currentItemIndex
            )
        } else if (entity.mediaId.isLocalPlaylist)
            LocalPlaylist.build(entity.mediaId, items.toMutableList(), entity.currentItemIndex)
        else
            TODO("Invalid playlist conversion media id ${entity.mediaId}")
    }.sortedBy(sorting)

    private fun transformHistory(
        entities: List<HistoryEntity>,
        songs: List<Song>,
        customizedSongs: List<CustomizedSong>
    ) = entities.mapNotNull { historyItem ->
        songs.find { historyItem.mediaId == it.mediaId }
            ?: customizedSongs.find { historyItem.mediaId == it.mediaId }
    }

    private suspend fun maybeUpdateEntityArtwork(entity: SongEntity?) {
        if (entity != null) {
            log.info("Updating entity: ${entity.title} - ${entity.artist} with ${entity.artworkType}")

            /**
             * This means that we found new artwork for a playback that hasn't been cached
             * in the database and thus needs to be cached now OR that there was cached artwork but
             * it was invalid and couldn't be resolved
             */
            songRepository.updateArtwork(
                entity.mediaId,
                entity.artworkType,
                entity.artworkUrl
            )
        }
    }
}