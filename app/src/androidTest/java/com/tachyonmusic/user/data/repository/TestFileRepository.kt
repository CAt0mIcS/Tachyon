package com.tachyonmusic.user.data.repository

import com.tachyonmusic.core.data.playback.LocalSong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.user.domain.FileRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

class TestFileRepository : FileRepository {
    override val songs: Deferred<ArrayList<Song>> = CompletableDeferred<ArrayList<Song>>().apply {
        val list = MutableList(10) { i ->
            LocalSong(MediaId(i.toString()), "Title:$i", "Artist:$i", i * 1000L) as Song
        } as ArrayList

        complete(list)
    }
}