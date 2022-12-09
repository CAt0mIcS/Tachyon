package com.tachyonmusic.presentation.library

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import androidx.paging.map
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    getSongs: GetPagedSongs,
    getLoops: GetLoops,
    getPlaylists: GetPlaylists,
    private val itemClicked: ItemClicked
) : ViewModel() {

    private var songs = getSongs(5, 0)
    private val loops = getLoops()
    private val playlists = getPlaylists()

    var items: Flow<PagingData<Playback>> = songs.map { it.map { song -> song } }
        private set

    fun onFilterSongs() {
        items = songs.map { it.map { song -> song } }
    }

    fun onFilterLoops() {
//        items = loops
        items = emptyFlow()
    }

    fun onFilterPlaylists() {
//        items = playlists
        items = emptyFlow()
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }
}