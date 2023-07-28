package com.tachyonmusic.core.domain.playback

interface Song : SinglePlayback {
    val isHidden: Boolean

    override fun copy(): Song
}
