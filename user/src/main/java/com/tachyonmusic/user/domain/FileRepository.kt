package com.tachyonmusic.user.domain

import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.flow.StateFlow

interface FileRepository {
    val songs: StateFlow<List<Song>>
    operator fun plusAssign(song: Song)
    operator fun minusAssign(song: Song)
}