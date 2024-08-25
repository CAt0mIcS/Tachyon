package com.tachyonmusic.playback_layers.data

import android.content.Context
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.playback_layers.SortingPreferences
import com.tachyonmusic.playback_layers.checkIfPlayable
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.sortedBy
import com.tachyonmusic.playback_layers.toPlayback
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
                entity.toPlayback(
                    when (entity.artworkType) {
                        ArtworkType.REMOTE -> RemoteArtwork(URI(entity.artworkUrl))
                        ArtworkType.EMBEDDED -> EmbeddedArtwork(null, entity.mediaId.uri!!)
                        else -> null
                    },
                    entity.checkIfPlayable(context)
                )
            } else
                TODO("Invalid media id ${entity.mediaId}")
        }.sortedBy(sorting)

    private fun transformRemixes(
        entities: List<RemixEntity>,
        songs: List<Playback>,
        sorting: SortingPreferences
    ): List<Playback> {
        assert(songs.all { it.isSong })

        return entities.mapNotNull { entity ->
            entity.toPlayback(songs.find { it.mediaId == entity.mediaId.underlyingMediaId }
                ?: return@mapNotNull null)
        }.sortedBy(sorting)
    }

    private fun transformPlaylists(
        entities: List<PlaylistEntity>,
        songs: List<Playback>,
        remixes: List<Playback>,
        sorting: SortingPreferences
    ): List<Playlist> {
        assert(songs.all { it.isSong })
        assert(remixes.all { it.isRemix })

        return entities.map { entity ->
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
                Playlist(
                    entity.mediaId,
                    items,
                    entity.currentItemIndex,
                    entity.timestampCreatedAddedEdited
                )
            else
                TODO("Invalid playlist conversion media id ${entity.mediaId}")
        }.sortedBy(sorting)
    }

    private fun transformHistory(
        entities: List<HistoryEntity>,
        songs: List<Playback>,
        remixes: List<Playback>
    ): List<Playback> {
        assert(songs.all { it.isSong })
        assert(remixes.all { it.isRemix })

        return entities.mapNotNull { historyItem ->
            songs.find { historyItem.mediaId == it.mediaId }
                ?: remixes.find { historyItem.mediaId == it.mediaId }
        }
    }
}