package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.GetPagedLoops
import com.tachyonmusic.domain.use_case.GetPagedPlaylists
import com.tachyonmusic.domain.use_case.GetPagedSongs
import com.tachyonmusic.domain.use_case.ItemClicked
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getSongs: GetPagedSongs,
    getLoops: GetPagedLoops,
    getPlaylists: GetPagedPlaylists,
    private val itemClicked: ItemClicked
) : ViewModel() {

    private var songs = getSongs(5)
    private val loops = getLoops(5)
    private val playlists = getPlaylists(5)

    var items: Flow<PagingData<Playback>> = songs.map { it.map { it } }
        private set

    fun onFilterSongs() {
        items = songs.map { it.map { it } }
    }

    fun onFilterLoops() {
        items = loops.map { it.map { it } }
    }

    fun onFilterPlaylists() {
        items = playlists.map { it.map { it } }
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }
}