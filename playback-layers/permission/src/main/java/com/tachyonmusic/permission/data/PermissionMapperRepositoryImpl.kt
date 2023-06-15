package com.tachyonmusic.permission.data

import android.content.Context
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
import com.tachyonmusic.permission.checkIfPlayable
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.permission.domain.UriPermissionRepository
import com.tachyonmusic.permission.toCustomizedSong
import com.tachyonmusic.permission.toPlaylist
import com.tachyonmusic.permission.toSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class PermissionMapperRepositoryImpl(
    uriPermissionRepository: UriPermissionRepository,

    private val songRepository: SongRepository,
    private val customizedSongRepository: CustomizedSongRepository,
    private val playlistRepository: PlaylistRepository,

    private val historyRepository: HistoryRepository,

    private val context: Context
) : PermissionMapperRepository {

    override val songFlow =
        combine(uriPermissionRepository.permissions, songRepository.observe()) { _, songs ->
            transformSongs(songs)
        }

    override val customizedSongFlow =
        combine(
            uriPermissionRepository.permissions,
            customizedSongRepository.observe()
        ) { _, customizedSongs ->
            transformCustomizedSongs(customizedSongs)
        }


    override val playlistFlow =
        combine(
            uriPermissionRepository.permissions,
            playlistRepository.observe(),
            songFlow,
            customizedSongFlow
        ) { _, playlists, songs, customizedSongs ->
            transformPlaylists(playlists, songs, customizedSongs)
        }

    override val historyFlow =
        combine(
            uriPermissionRepository.permissions,
            historyRepository.observe(),
            songFlow,
            customizedSongFlow
        ) { _, history, songs, customizedSongs ->
            transformHistory(history, songs, customizedSongs)
        }

    override suspend fun getSongs() = withContext(Dispatchers.IO) {
        transformSongs(songRepository.getSongs())
    }

    override suspend fun getCustomizedSongs() = withContext(Dispatchers.IO) {
        transformCustomizedSongs(customizedSongRepository.getCustomizedSongs())
    }

    override suspend fun getPlaylists() = withContext(Dispatchers.IO) {
        transformPlaylists(playlistRepository.getPlaylists(), getSongs(), getCustomizedSongs())
    }

    override suspend fun getHistory() = withContext(Dispatchers.IO) {
        transformHistory(historyRepository.getHistory(), getSongs(), getCustomizedSongs())
    }


    private fun transformSongs(songs: List<SongEntity>) = songs.map {
        it.toSong(it.checkIfPlayable(context))
    }

    private fun transformCustomizedSongs(customizedSongs: List<CustomizedSongEntity>) =
        customizedSongs.map {
            it.toCustomizedSong(it.checkIfPlayable(context))
        }

    private fun transformPlaylists(
        playlists: List<PlaylistEntity>,
        songs: List<Song>,
        customizedSongs: List<CustomizedSong>
    ) = playlists.map { playlist ->
        playlist.toPlaylist(playlist.items.mapNotNull { playlistItem ->
            if (playlistItem.isLocalSong || playlistItem.isSpotifySong) {
                songs.find { playlistItem == it.mediaId }
            } else if(playlistItem.isLocalCustomizedSong) {
                customizedSongs.find { playlistItem == it.mediaId }
            }
            else {
                TODO("Invalid playlist item $playlistItem")
            }
        })
    }

    private fun transformHistory(
        singlePbs: List<HistoryEntity>,
        songs: List<Song>,
        customizedSongs: List<CustomizedSong>
    ) = singlePbs.mapNotNull { historyItem ->
        songs.find { historyItem.mediaId == it.mediaId }
            ?: customizedSongs.find { historyItem.mediaId == it.mediaId }
    }
}