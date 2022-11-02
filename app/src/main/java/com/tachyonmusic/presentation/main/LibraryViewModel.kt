package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.main.AddPlaybackUseCases
import com.tachyonmusic.domain.use_case.main.GetPlaybacksUseCases
import com.tachyonmusic.domain.use_case.main.ItemClicked
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    getPlaybacks: GetPlaybacksUseCases,
    private val addPlayback: AddPlaybackUseCases
) : ViewModel() {

    val songs = getPlaybacks.songs()
    val loops = getPlaybacks.loops()
    val playlist = getPlaybacks.playlists()


    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }

    fun addItem() {
        addPlayback.addSong(
            LocalSong(MediaId("TestSource"), "title", "artist", 123L)
        )
    }

}