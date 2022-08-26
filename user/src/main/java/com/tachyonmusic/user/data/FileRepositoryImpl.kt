package com.tachyonmusic.user.data

import com.tachyonmusic.core.domain.model.*
import com.tachyonmusic.user.domain.FileRepository

class FileRepositoryImpl : FileRepository {
    override val songs: MutableList<Song> = mutableListOf()
    override val loops: MutableList<Loop> = mutableListOf()
    override val playlists: MutableList<Playlist> = mutableListOf()

    override fun find(mediaId: MediaId): Playback? {
        val s = songs.find { it.mediaId == mediaId }
        if (s != null)
            return s
        val l = loops.find { it.mediaId == mediaId }
        if (l != null)
            return l
        return playlists.find { it.mediaId == mediaId }
    }
}