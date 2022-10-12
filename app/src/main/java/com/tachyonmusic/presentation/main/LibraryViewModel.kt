package com.tachyonmusic.presentation.main

import androidx.lifecycle.ViewModel
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.domain.use_case.main.ItemClicked
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val itemClicked: ItemClicked
) : ViewModel() {

    fun onItemClicked(playback: Playback) {
        itemClicked(playback)
    }

}