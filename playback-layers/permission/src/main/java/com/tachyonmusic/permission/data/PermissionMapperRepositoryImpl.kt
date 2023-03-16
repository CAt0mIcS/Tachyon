package com.tachyonmusic.permission.data

import android.content.Context
import com.tachyonmusic.database.domain.model.HistoryEntity
import com.tachyonmusic.database.domain.model.LoopEntity
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.permission.checkIfPlayable
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.UriPermissionRepository
import com.tachyonmusic.permission.domain.model.LoopPermissionEntity
import com.tachyonmusic.permission.domain.model.SongPermissionEntity
import com.tachyonmusic.permission.domain.model.toPermissionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class PermissionMapperRepositoryImpl(
    uriPermissionRepository: UriPermissionRepository,

    private val songRepository: SongRepository,
    private val loopRepository: LoopRepository,
    private val playlistRepository: PlaylistRepository,

    private val historyRepository: HistoryRepository,

    private val context: Context
) : PermissionMapperRepository {

    override val songFlow =
        combine(uriPermissionRepository.permissions, songRepository.observe()) { _, songs ->
            transformSongs(songs)
        }

    override val loopFlow =
        combine(uriPermissionRepository.permissions, loopRepository.observe()) { _, loops ->
            transformLoops(loops)
        }


    override val playlistFlow =
        combine(
            uriPermissionRepository.permissions,
            playlistRepository.observe(),
            songFlow,
            loopFlow
        ) { _, playlists, songs, loops ->
            transformPlaylists(playlists, songs, loops)
        }

    override val historyFlow =
        combine(
            uriPermissionRepository.permissions,
            historyRepository.observe(),
            songFlow,
            loopFlow
        ) { _, history, songs, loops ->
            transformHistory(history, songs, loops)
        }

    override suspend fun getSongs() = withContext(Dispatchers.IO) {
        transformSongs(songRepository.getSongs())
    }

    override suspend fun getLoops() = withContext(Dispatchers.IO) {
        transformLoops(loopRepository.getLoops())
    }

    override suspend fun getPlaylists() = withContext(Dispatchers.IO) {
        transformPlaylists(playlistRepository.getPlaylists(), getSongs(), getLoops())
    }

    override suspend fun getHistory() = withContext(Dispatchers.IO) {
        transformHistory(historyRepository.getHistory(), getSongs(), getLoops())
    }


    private fun transformSongs(songs: List<SongEntity>) = songs.map {
        it.toPermissionEntity(it.checkIfPlayable(context))
    }

    private fun transformLoops(loops: List<LoopEntity>) = loops.map {
        it.toPermissionEntity(it.checkIfPlayable(context))
    }

    private fun transformPlaylists(
        playlists: List<PlaylistEntity>,
        songs: List<SongPermissionEntity>,
        loops: List<LoopPermissionEntity>
    ) = playlists.map { playlist ->
        playlist.toPermissionEntity(playlist.items.mapNotNull { playlistItem ->
            songs.find { playlistItem == it.mediaId }
                ?: loops.find { playlistItem == it.mediaId }
        })
    }

    private fun transformHistory(
        singlePbs: List<HistoryEntity>,
        songs: List<SongPermissionEntity>,
        loops: List<LoopPermissionEntity>
    ) = singlePbs.mapNotNull { historyItem ->
        songs.find { historyItem.mediaId == it.mediaId }
            ?: loops.find { historyItem.mediaId == it.mediaId }
    }
}