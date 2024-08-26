package com.tachyonmusic.playback_layers.data

import android.content.Context
import android.net.Uri
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.RemixEntity
import com.tachyonmusic.database.domain.model.SinglePlaybackEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.RemixRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.playback_layers.SortingPreferences
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.UriPermissionRepository
import com.tachyonmusic.playback_layers.sortedBy
import com.tachyonmusic.playback_layers.toPlayback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.io.FileNotFoundException
import java.net.URI

class PlaybackRepositoryImpl(
    private val songRepository: SongRepository,
    private val remixRepository: RemixRepository,
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,

    uriPermissionRepository: UriPermissionRepository,

    private val context: Context,
) : PlaybackRepository {

    // PlaybackRepository is alive until the end of the program, so it doesn't need to be cancelled
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cacheLock = Any()
    private val permissionCache = mutableMapOf<Uri, Boolean>()

    init {
        // Invalidate the isPlayable cache every time the permissions change
        uriPermissionRepository.permissions.onEach {
            synchronized(cacheLock) {
                permissionCache.clear()
            }
        }.launchIn(ioScope)
    }

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
    

    private fun Uri.isPlayable(context: Context) = try {
        context.contentResolver.openInputStream(this)?.close() ?: false
        true
    } catch (e: Exception) {
        when(e) {
            is FileNotFoundException, is IllegalArgumentException -> false
            else -> throw e
        }
    }

    private fun SinglePlaybackEntity.checkIfPlayable(context: Context): Boolean {
        val key = mediaId.uri ?: return false

        return synchronized(cacheLock) {
            permissionCache.getOrPut(key) {
                key.isPlayable(context)
            }
        }
    }
}