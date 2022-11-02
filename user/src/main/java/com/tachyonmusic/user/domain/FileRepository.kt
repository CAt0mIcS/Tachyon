package com.tachyonmusic.user.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.StateFlow

interface FileRepository {
    val songs: StateFlow<List<Song>>
    operator fun plusAssign(song: Song)
    operator fun minusAssign(song: Song)
}