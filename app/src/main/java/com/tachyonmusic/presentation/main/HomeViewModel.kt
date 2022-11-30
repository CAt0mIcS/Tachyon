package com.tachyonmusic.presentation.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.main.GetCurrentPositionNormalized
import com.tachyonmusic.domain.use_case.main.GetHistory
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.PlayerListenerHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getSongs: GetSongs,
    getLoops: GetLoops,
    getPlaylists: GetPlaylists,
    getHistory: GetHistory,
    private val playerListener: PlayerListenerHandler,
    private val getAudioUpdateInterval: GetAudioUpdateInterval,
    private val loadPlaybackArtwork: LoadPlaybackArtwork,
) : ViewModel() {

    val songs = getSongs()
    val loops = getLoops()
    val playlists = getPlaylists()

    val isPlaying = playerListener.isPlaying
    val currentPositionNormalized: Float
        get() = .5f
    val audioUpdateInterval: Duration
        get() = getAudioUpdateInterval()

//    val history = getHistory()

    val history = MutableStateFlow(listOf<Playback>())

    private val _recentlyPlayed = mutableStateOf<Playback?>(null)
    val recentlyPlayed: State<Playback?> = _recentlyPlayed


    init {
        if (songs.value.isNotEmpty()) {
            history.value = mutableListOf<Playback>().apply { addAll(songs.value) }

            viewModelScope.launch(Dispatchers.IO) {
//            loadPlaybackArtwork(items).map { res ->
//                when (res) {
//                    is Resource.Loading -> albumArtworkLoading[res.data!!] = true
//                    is Resource.Error, is Resource.Success -> albumArtworkLoading[res.data!!] =
//                        false
//                }
//            }.collect()

                loadPlaybackArtwork(history.value as List<Song>).map { res ->
                }.collect()
            }

            _recentlyPlayed.value =
                history.value.find { (it as LocalSongImpl).path.absolutePath == "/storage/emulated/0/Music/Don't Play - JAEGER.mp3" }
                    ?: history.value[0]
        }
    }

    fun registerPlayerListener() {
        playerListener.register()
    }

    fun unregisterPlayerListener() {
        playerListener.unregister()
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