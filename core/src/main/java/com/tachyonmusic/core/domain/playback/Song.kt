package com.tachyonmusic.core.domain.playback

interface Song : SinglePlayback {
    override fun copy(): Song
}
