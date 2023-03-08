package com.tachyonmusic.permission.data

import android.content.Context
import com.tachyonmusic.database.domain.repository.HistoryRepository
import com.tachyonmusic.database.domain.repository.LoopRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.permission.checkIfPlayable
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.UriPermissionRepository
import com.tachyonmusic.permission.domain.model.toPermissionEntity
import kotlinx.coroutines.flow.combine

class PermissionMapperRepositoryImpl(
    uriPermissionRepository: UriPermissionRepository,

    songRepository: SongRepository,
    loopRepository: LoopRepository,
    playlistRepository: PlaylistRepository,

    historyRepository: HistoryRepository,

    private val context: Context
) : PermissionMapperRepository {

    override val songs =
        combine(uriPermissionRepository.permissions, songRepository.observe()) { _, songs ->
            songs.map {
                it.toPermissionEntity(it.checkIfPlayable(context))
            }
        }

    override val loops =
        combine(uriPermissionRepository.permissions, loopRepository.observe()) { _, loops ->
            loops.map {
                it.toPermissionEntity(it.checkIfPlayable(context))
            }
        }


    override val playlists =
        combine(
            uriPermissionRepository.permissions,
            playlistRepository.observe(),
            songs,
            loops
        ) { _, playlists, songs, loops ->
            playlists.map { playlist ->
                playlist.toPermissionEntity(playlist.items.mapNotNull { playlistItem ->
                    songs.find { playlistItem == it.mediaId }
                        ?: loops.find { playlistItem == it.mediaId }
                })
            }
        }

    override val history =
        combine(
            uriPermissionRepository.permissions,
            historyRepository.observe(),
            songs,
            loops
        ) { _, history, songs, loops ->
            history.mapNotNull { historyItem ->
                songs.find { historyItem.mediaId == it.mediaId }
                    ?: loops.find { historyItem.mediaId == it.mediaId }
            }
        }
}