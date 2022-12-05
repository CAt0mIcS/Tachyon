package com.tachyonmusic.presentation.main

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.playback.LocalSongImpl
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.domain.use_case.*
import com.tachyonmusic.domain.use_case.main.GetCurrentPositionNormalized
import com.tachyonmusic.domain.use_case.main.GetHistory
import com.tachyonmusic.domain.use_case.player.GetAudioUpdateInterval
import com.tachyonmusic.domain.use_case.player.PauseResumePlayback
import com.tachyonmusic.domain.use_case.player.PlayerListenerHandler
import com.tachyonmusic.domain.use_case.player.SetCurrentPlayback
import com.tachyonmusic.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val setCurrentPlayback: SetCurrentPlayback,
    private val pauseResumePlayback: PauseResumePlayback,
    private val getCurrentPositionNormalized: GetCurrentPositionNormalized,
    private val application: Application // TODO: Shouldn't be here
) : ViewModel() {

    val songs = getSongs()
    val loops = getLoops()
    val playlists = getPlaylists()

    val isPlaying = playerListener.isPlaying
    val currentPositionNormalized: Float
        get() = getCurrentPositionNormalized()
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
                for (song in history.value.filterIsInstance<Song>()) {
                    song.loadArtwork(100).map {
                        when (it) {
                            is Resource.Error -> Log.e(
                                "HomeViewModel",
                                "${song.title} - ${song.artist}: ${it.message!!.asString(application)}"
                            )
                            is Resource.Success -> Log.d(
                                "HomeViewModel",
                                "Successfully loaded cover art for ${song.title} - ${song.artist}"
                            )
                            else -> {}
                        }
                    }.collect()
                }
                Log.d("HomeViewModel", "Finished loading song artwork")
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

    fun onPlayPauseClicked(playback: Playback?) {
        if (isPlaying.value)
            pauseResumePlayback(PauseResumePlayback.Action.Pause)
        else if (!setCurrentPlayback(playback))
            pauseResumePlayback(PauseResumePlayback.Action.Resume)
    }

    fun onMiniPlayerClicked(playback: Playback?) {
        setCurrentPlayback(playback, false)
    }
}