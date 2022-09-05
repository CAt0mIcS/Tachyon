package com.tachyonmusic.user.domain

import com.tachyonmusic.core.domain.playback.Song
import kotlinx.coroutines.Deferred

interface FileRepository {
    val songs: Deferred<ArrayList<Song>>

    suspend operator fun plusAssign(song: Song) {
        songs.await().add(song)
        songs.await().sortBy { it.title + it.artist }
    }
}