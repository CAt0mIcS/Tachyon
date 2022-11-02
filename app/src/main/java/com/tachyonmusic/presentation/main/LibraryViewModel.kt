package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.use_case.main.ItemClicked
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val itemClicked: ItemClicked,
    private val browser: MediaBrowserController
) : ViewModel() {

    val songs
        get() = browser.songs
    val loops
        get() = browser.loops
    val playlist
        get() = browser.playlists


    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }

    fun addItem() {
        browser += LocalSong(MediaId("TestSource"), "title", "artist", 123L)
    }

}