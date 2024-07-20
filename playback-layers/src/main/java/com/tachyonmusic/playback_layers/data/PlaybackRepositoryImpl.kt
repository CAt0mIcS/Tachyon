package com.tachyonmusic.playback_layers.data

import android.content.Context
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.playback_layers.SortingPreferences
import com.tachyonmusic.playback_layers.checkIfPlayable
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.sortedBy
import com.tachyonmusic.playback_layers.toRemix
import com.tachyonmusic.playback_layers.toLocalSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.net.URI

class PlaybackRepositoryImpl(
    private val songRepository: SongRepository,
    private val remixRepository: RemixRepository,
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

    override val remixFlow =
        combine(
            remixRepository.observe(),
            songFlow,
            sortingPreferences
        ) { remixEntities, songs, sorting ->
            transformRemixes(remixEntities, songs, sorting)
        }

    override val playlistFlow = combine(
        playlistRepository.observe(),
        songFlow,
        remixFlow,
        sortingPreferences
    ) { playlistEntities, songs, remixes, sorting ->
        transformPlaylists(playlistEntities, songs, remixes, sorting)
    }

    override val historyFlow = combine(
        historyRepository.observe(),
        songFlow,
        remixFlow
    ) { historyEntities, songs, remixes ->
        transformHistory(historyEntities, songs, remixes)
    }

    override suspend fun getSongs() =
        transformSongs(songRepository.getSongs(), sortingPreferences.value)

    override suspend fun getRemixes() =
        transformRemixes(
            remixRepository.getRemixes(),
            getSongs(),
            sortingPreferences.value
        )

    override suspend fun getPlaylists() =
        transformPlaylists(
            playlistRepository.getPlaylists(),
            getSongs(),
            getRemixes(),
            sortingPreferences.value
        )

    override suspend fun getHistory() =
        transformHistory(historyRepository.getHistory(), getSongs(), getRemixes())

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
            }
            else
                TODO("Invalid media id ${entity.mediaId}")
        }.sortedBy(sorting)

    private fun transformRemixes(
        entities: List<RemixEntity>,
        songs: List<Song>,
        sorting: SortingPreferences
    ): List<Remix> =
        entities.map { entity ->
            entity.toRemix(songs.find { it.mediaId == entity.mediaId.underlyingMediaId })
        }.sortedBy(sorting)

    private fun transformPlaylists(
        entities: List<PlaylistEntity>,
        songs: List<Song>,
        remixes: List<Remix>,
        sorting: SortingPreferences
    ) = entities.map { entity ->
        // TODO: Somehow display deleted songs and remixes
        val items = entity.items.mapNotNull { playlistItem ->
            if (playlistItem.isLocalSong) {
                songs.find { playlistItem == it.mediaId }
            } else if (playlistItem.isLocalRemix) {
                remixes.find { playlistItem == it.mediaId }
            } else {
                TODO("Invalid playlist item $playlistItem")
            }
        }

        if (entity.mediaId.isLocalPlaylist)
            LocalPlaylist.build(entity.mediaId, items.toMutableList(), entity.currentItemIndex)
        else
            TODO("Invalid playlist conversion media id ${entity.mediaId}")
    }.sortedBy(sorting)

    private fun transformHistory(
        entities: List<HistoryEntity>,
        songs: List<Song>,
        remixes: List<Remix>
    ) = entities.mapNotNull { historyItem ->
        songs.find { historyItem.mediaId == it.mediaId }
            ?: remixes.find { historyItem.mediaId == it.mediaId }
    }
}