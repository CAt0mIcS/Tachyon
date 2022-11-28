package com.tachyonmusic.presentation.main

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.main.GetHistory
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getSongs: GetSongs,
    getLoops: GetLoops,
    getPlaylists: GetPlaylists,
    getHistory: GetHistory,
    private val loadPlaybackArtwork: LoadPlaybackArtwork,
) : ViewModel() {

    val songs = getSongs()
    val loops = getLoops()
    val playlists = getPlaylists()

//    val history = getHistory()

    val history = MutableStateFlow(listOf<Playback>())


    init {
        val song = songs.value.find { it.title == "Don't Play" }!!
        history.value = listOf(
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song,
            song
        )

        viewModelScope.launch(Dispatchers.IO) {
//            loadPlaybackArtwork(items).map { res ->
//                when (res) {
//                    is Resource.Loading -> albumArtworkLoading[res.data!!] = true
//                    is Resource.Error, is Resource.Success -> albumArtworkLoading[res.data!!] =
//                        false
//                }
//            }.collect()

            loadPlaybackArtwork(listOf(history.value[0] as Song)).map { res ->
            }.collect()
        }
    }

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)

        // TODO: Unload artwork in not used songs to save memory
        // TODO: Don't unload now playing artwork, currently is reloaded again in [PlayerViewModel]
//        for (song in songs.value) {
//            song.unloadArtwork()
//        }
    }
}