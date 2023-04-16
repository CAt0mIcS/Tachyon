package com.tachyonmusic.core.domain.playback

interface CustomizedSong : SinglePlayback {
    val name: String
    val song: Song

    override fun copy(): CustomizedSong
}
