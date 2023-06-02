package com.tachyonmusic.sort.data

import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.core.domain.playback.Playlist
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.permission.domain.PermissionMapperRepository
import com.tachyonmusic.sort.domain.SortedPlaybackRepository
import com.tachyonmusic.sort.domain.model.SortingPreferences
import com.tachyonmusic.sort.domain.model.sortedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class SortedPlaybackRepositoryImpl(
    private val permissionRepository: PermissionMapperRepository
) : SortedPlaybackRepository {

    /**
     * TODO
     *  Sometimes out of sync: Playlist could still be loaded after UI restart, sortingPreferences will be
     *  defaulted but the loaded playlist will have different sorting
     *  When changing sortingPreferences the currently playing playlist should also be updated in the player
     */
    private val _sortingPreferences = MutableStateFlow(SortingPreferences())
    override val sortingPreferences = _sortingPreferences.asStateFlow()

    override val songFlow =
        combine(permissionRepository.songFlow, sortingPreferences) { songs, sortPrefs ->
            transformSongs(songs, sortPrefs)
        }

    override val customizedSongFlow =
        combine(permissionRepository.customizedSongFlow, sortingPreferences) { customizedSongs, sortPrefs ->
            transformCustomizedSongs(customizedSongs, sortPrefs)
        }

    override val playlistFlow =
        combine(permissionRepository.playlistFlow, sortingPreferences) { playlists, sortPrefs ->
            transformPlaylists(playlists, sortPrefs)
        }

    override val historyFlow = permissionRepository.historyFlow


    override fun setSortingPreferences(sortPrefs: SortingPreferences) {
        _sortingPreferences.update { sortPrefs }
    }

    override suspend fun getSongs() =
        transformSongs(permissionRepository.getSongs(), sortingPreferences.value)

    override suspend fun getCustomizedSongs() =
        transformCustomizedSongs(permissionRepository.getCustomizedSongs(), sortingPreferences.value)

    override suspend fun getPlaylists() =
        transformPlaylists(permissionRepository.getPlaylists(), sortingPreferences.value)

    override suspend fun getHistory() = permissionRepository.getHistory()


    private fun transformSongs(songs: List<Song>, sortPrefs: SortingPreferences): List<Song> {
        return songs.sortedBy(sortPrefs)
    }

    private fun transformCustomizedSongs(customizedSongs: List<CustomizedSong>, sortPrefs: SortingPreferences): List<CustomizedSong> {
        return customizedSongs.sortedBy(sortPrefs)
    }

    private fun transformPlaylists(
        playlists: List<Playlist>,
        sortPrefs: SortingPreferences
    ): List<Playlist> {
        return playlists.sortedBy(sortPrefs)
    }
}