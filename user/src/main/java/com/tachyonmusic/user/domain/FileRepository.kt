package com.tachyonmusic.user.domain

import com.tachyonmusic.core.domain.model.*

interface FileRepository {
    val songs: MutableList<Song>
    val loops: MutableList<Loop>
    val playlists: MutableList<Playlist>

    fun find(mediaId: MediaId): Playback?
}