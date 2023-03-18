package com.tachyonmusic.sort.data

import com.tachyonmusic.core.domain.playback.Loop
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.sort.domain.SortedPlaybackRepository
import com.tachyonmusic.sort.domain.model.SortingPreferences
import com.tachyonmusic.sort.domain.model.sortedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class SortedPlaybackRepositoryImpl(
    private val permissionRepository: PermissionMapperRepository
) : SortedPlaybackRepository {
    private val _sortingPreferences = MutableStateFlow(SortingPreferences())
    override val sortingPreferences = _sortingPreferences.asStateFlow()

    override val songFlow = permissionRepository.songFlow.map {
        transformSongs(it)
    }

    override val loopFlow = permissionRepository.loopFlow.map {
        transformLoops(it)
    }

    override val playlistFlow = permissionRepository.playlistFlow.map {
        transformPlaylists(it)
    }

    override val historyFlow = permissionRepository.historyFlow


    override fun setSortingPreferences(sortPrefs: SortingPreferences) {
        _sortingPreferences.update { sortPrefs }
    }

    override suspend fun getSongs() = transformSongs(permissionRepository.getSongs())
    override suspend fun getLoops() = transformLoops(permissionRepository.getLoops())
    override suspend fun getPlaylists() = transformPlaylists(permissionRepository.getPlaylists())
    override suspend fun getHistory() = permissionRepository.getHistory()


    private fun transformSongs(songs: List<Song>): List<Song> {
        return songs.sortedBy(sortingPreferences.value ?: return songs)
    }

    private fun transformLoops(loops: List<Loop>): List<Loop> {
        return loops.sortedBy(sortingPreferences.value ?: return loops)
    }

    private fun transformPlaylists(playlists: List<Playlist>): List<Playlist> {
        return playlists.sortedBy(sortingPreferences.value ?: return playlists)
    }
}