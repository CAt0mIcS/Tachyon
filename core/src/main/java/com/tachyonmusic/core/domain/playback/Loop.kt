package com.tachyonmusic.core.domain.playback

interface Loop : SinglePlayback {
    val name: String
    val song: Song
}
