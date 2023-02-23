package com.tachyonmusic.media.util

import com.tachyonmusic.core.data.constants.Constants
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.util.File
import com.tachyonmusic.util.ms

internal fun getSongs() = MutableList(50) { i ->
    return@MutableList when {
        i < 10 -> oneSong(i, 0)
        i < 20 -> oneSong(i, 1)
        i < 30 -> oneSong(i, 2)
        else -> oneSong(i, 3)
    }
}


internal fun getLoops() = MutableList(50) { i ->
    val name = "Loop $i"
    return@MutableList when {
        i < 10 -> oneLoop(name, i, 0)
        i < 20 -> oneLoop(name, i, 1)
        i < 30 -> oneLoop(name, i, 2)
        else -> oneLoop(name, i, 3)
    }
}

internal fun getPlaylists() = MutableList(50) { i ->
    val name = "Playlist $i"
    onePlaylist(name)
}


private fun oneSong(i: Int, artistI: Int): TestSong {
    val title = "Song $i of Artist $artistI"
    val artist = "Artist $artistI"
    return TestSong(
        MediaId.ofLocalSong(File(Constants.EXTERNAL_STORAGE_DIRECTORY + title + artist)),
        title,
        artist,
        (i.toLong() * 1000L).ms
    )
}

private fun oneLoop(name: String, i: Int, artistI: Int): TestLoop {
    val song = oneSong(i, artistI)
    return TestLoop(MediaId.ofRemoteLoop(name, song.mediaId), name, song)
}

private fun onePlaylist(name: String): TestPlaylist {
    return TestPlaylist(MediaId.ofRemotePlaylist(name), name, List(20) { i ->
        when {
            i < 5 -> oneSong(i, 0)
            i < 10 -> oneLoop("Loop $i", i, 0)
            i < 15 -> oneSong(i, 1)
            else -> oneLoop("Loop $i", i, 1)
        }
    }.toMutableList())
}