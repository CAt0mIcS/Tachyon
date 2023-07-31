package com.tachyonmusic.playback_layers.data

import android.content.Context
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.data.playback.SpotifyPlaylist
import com.tachyonmusic.core.data.playback.SpotifySong
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
import com.tachyonmusic.playback_layers.SortingPreferences
import com.tachyonmusic.playback_layers.checkIfPlayable
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.sortedBy
import com.tachyonmusic.playback_layers.toCustomizedSong
import com.tachyonmusic.playback_layers.toLocalSong
import com.tachyonmusic.playback_layers.toSpotifySong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.net.URI

class PlaybackRepositoryImpl(
    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,

    private val context: Context,
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

    private fun transformSongs(entities: List<SongEntity>, sorting: SortingPreferences) =
        entities.map { entity ->
            if (entity.mediaId.isLocalSong) {
                entity.toLocalSong(
                    when (entity.artworkType) {
                        ArtworkType.REMOTE -> RemoteArtwork(URI(entity.artworkUrl))
                        ArtworkType.EMBEDDED -> EmbeddedArtwork(null, entity.mediaId.uri!!)
                        else -> null
                    },
                    entity.checkIfPlayable(context)
                )
            } else if (entity.mediaId.isSpotifySong)
                entity.toSpotifySong()
            else
                TODO("Invalid media id ${entity.mediaId}")
        }.sortedBy(sorting)

    private fun transformCustomizedSongs(
        entities: List<CustomizedSongEntity>,
        songs: List<Song>,
        sorting: SortingPreferences
    ): List<CustomizedSong> =
        entities.map { entity ->
            entity.toCustomizedSong(songs.find { it.mediaId == entity.mediaId.underlyingMediaId })
        }.sortedBy(sorting)

    private fun transformPlaylists(
        entities: List<PlaylistEntity>,
        songs: List<Song>,
        customizedSongs: List<CustomizedSong>,
        sorting: SortingPreferences
    ) = entities.map { entity ->
        // TODO: Somehow display deleted songs and customized songs
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
}